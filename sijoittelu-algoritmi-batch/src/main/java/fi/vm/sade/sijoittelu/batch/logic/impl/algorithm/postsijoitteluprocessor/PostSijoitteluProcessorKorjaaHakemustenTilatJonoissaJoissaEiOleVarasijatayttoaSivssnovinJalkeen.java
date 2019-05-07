package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuHyvaksyttyihinTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutAloituspaikatTaynna;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYLATTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.PERUUNTUNUT;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.ALITAYTTO;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.ARVONTA;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.YLITAYTTO;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Valintatapajono.JonosijaTieto;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Tämä prosessori on ajettava ennen <code>PostSijoitteluProcessorAsetaSivssnov</code> :ia, jotta tämä voi tunnistaa
 * tilanteen, jossa ollaan ajamassa ensimmäistä sijoittelua sen jälkeen kun varasijasäännöt ovat astuneet voimaan.
 *
 * @see PostSijoitteluProcessorAsetaSivssnov
 */
public class PostSijoitteluProcessorKorjaaHakemustenTilatJonoissaJoissaEiOleVarasijatayttoaSivssnovinJalkeen implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorKorjaaHakemustenTilatJonoissaJoissaEiOleVarasijatayttoaSivssnovinJalkeen.class);

    @Override
    public void process(SijoitteluajoWrapper ajoWrapper) {
        if (!SivssnovUtil.varasijaSaannotVoimassa(ajoWrapper)) {
            return;
        }
        ajoWrapper.getHakukohteet().forEach(h -> h.getValintatapajonot().forEach(this::process));
    }

    private void process(ValintatapajonoWrapper jonoWrapper) {
        Valintatapajono jono = jonoWrapper.getValintatapajono();
        if (jono.vapaaVarasijataytto()) {
            return;
        }
        SijoitteluajoWrapper sijoitteluajoWrapper = jonoWrapper.getHakukohdeWrapper().getSijoitteluajoWrapper();
        SivssnovUtil.assertSijoiteltuEnnenVarasijataytonLoppumista(jonoWrapper, sijoitteluajoWrapper);

        if (Boolean.TRUE.equals(jono.getEiVarasijatayttoa()) && sijoitteluajoWrapper.isKKHaku()) {
            peruunnutaJonossaJossaEiVarasijatayttoa(jonoWrapper);
        }
    }

    private void peruunnutaJonossaJossaEiVarasijatayttoa(ValintatapajonoWrapper jonoWrapper) {
        Valintatapajono jono = jonoWrapper.getValintatapajono();
        if (jono.getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()) {
            if (jono.getSivssnovSijoittelunVarasijataytonRajoitus().isPresent()) {
                peruunnutaLeikkurisijanAllaOlevatJaJataHyvaksytyiksiNousevatvaralle(jono);
                return;
            } else {
                LOG.warn(String.format("Jonosta %s ei löytynyt tietoa alimmasta hyväksytystä, vaikka siinä ei ole varasijatäyttöä ja se on " +
                    "sijoiteltu ilman varasijasääntöjä niiden ollessa voimassa. Joko se on sijoiteltu vanhalla sovellusversiolla tai jossain " +
                    "on bugi tai outoa dataa. Peruunnutetaan kaikki varallolijat ja tallennetaan tieto alimmasta hyväksytystä.",
                    jono.getOid()));
            }
        }
        peruunnutaKaikkiVarallaolijat(jonoWrapper);
    }

    private void peruunnutaLeikkurisijanAllaOlevatJaJataHyvaksytyiksiNousevatvaralle(Valintatapajono jono) {
        JonosijaTieto raja = jono.getSivssnovSijoittelunVarasijataytonRajoitus().get();
        Assert.isTrue(kuuluuHyvaksyttyihinTiloihin(raja.tila), String.format("Jonolla %s ei ole varasijatäyttöä, " +
            "joten sen sivssnov-rajan pitäisi olla hyväksytty-tyyppinen, mutta se on %s", jono.getOid(), raja));
        final AtomicInteger hyvaksytyt = new AtomicInteger(0);

        jono.getHakemukset().stream().sorted(new HakemusComparator()).forEach(h -> {
            if (Arrays.asList(VARALLA, PERUUNTUNUT, HYLATTY).contains(h.getEdellinenTila()) &&
                kuuluuHyvaksyttyihinTiloihin(h.getTila()) && (h.getJonosija() <= raja.jonosija)) {
                LOG.info(String.format("Jätetään tilassa %s ollut hakemus %s (edellinen tila %s, jonosija/tasasijajonosija %s/%s) varalle jonossa %s, " +
                    "koska jonossa ei ole varasijatäyttöä, SIVSSNOV on päällä ja leikkurijonosija %s",
                    h.getTila(), h.getHakemusOid(), h.getEdellinenTila(), h.getJonosija(), h.getTasasijaJonosija(), jono.getOid(), raja));
                h.setTila(VARALLA);
            } else if (kuuluuHyvaksyttyihinTiloihin(h.getTila()) && hyvaksytyt.get() < jono.getAloituspaikat() && (h.getJonosija() <= raja.jonosija)) {
                if (ARVONTA.equals(jono.getTasasijasaanto())) {
                    hyvaksytyt.incrementAndGet();
                } else {
                    List<Hakemus> samanSijanHakemukset = jono.getHakemukset().stream().
                        filter(h2 -> h2.getJonosija().equals(h.getJonosija())).
                        collect(Collectors.toList());
                    if (YLITAYTTO.equals(jono.getTasasijasaanto())) {
                        hyvaksytyt.addAndGet(samanSijanHakemukset.size());
                    } else if (ALITAYTTO.equals(jono.getTasasijasaanto()) &&
                        (samanSijanHakemukset.size() <= jono.getAloituspaikat() - hyvaksytyt.get())) {
                        hyvaksytyt.addAndGet(samanSijanHakemukset.size());
                    } else {
                        LOG.info(String.format("Jono %s, jolla ei ole varasijatäyttöä ja jonka tasasijasääntö on %s : " +
                            "peruunnutetaan ylimääräiset hakemukset.", jono.getOid(), jono.getTasasijasaanto()));
                        samanSijanHakemukset.forEach(h2 -> {
                            LOG.info(String.format("Merkitään hakemus %s peruuntuneeksi jonossa %s, jolla ei ole varasijatäyttöä " +
                                "ja jonka tasasijasääntö on %s", h2.getHakemusOid(), jono.getOid(), jono.getTasasijasaanto()));
                            asetaTilaksiPeruuntunutAloituspaikatTaynna(h2);
                        });
                    }
                }
            } else if (VARALLA.equals(h.getTila()) || kuuluuHyvaksyttyihinTiloihin(h.getTila()) && h.getJonosija() >= raja.jonosija) {
                LOG.info(String.format("Siirretään tilassa %s ollut hakemus %s peruuntuneeksi jonossa %s, jolla ei ole varasijatäyttöä " +
                    "ja jonka tasasijasääntö on %s, koska hakemuksen jonosija on %d ja leikkurijonosija on %s", h.getTila(),
                    h.getHakemusOid(), jono.getOid(), jono.getTasasijasaanto(), h.getJonosija(), raja));
                asetaTilaksiPeruuntunutAloituspaikatTaynna(h);
            }
        });
    }

    private void peruunnutaKaikkiVarallaolijat(ValintatapajonoWrapper jonoWrapper) {
        List<Hakemus> kaikkiHyvaksytyt = jonoWrapper.getHakemukset().stream()
            .filter(hw -> kuuluuHyvaksyttyihinTiloihin(hw.getHakemus().getTila()))
            .sorted(new HakemusWrapperComparator())
            .map(HakemusWrapper::getHakemus)
            .collect(Collectors.toList());
        Collections.reverse(kaikkiHyvaksytyt);
        Optional<JonosijaTieto> alinHyvaksyttyJonosija = kaikkiHyvaksytyt.stream()
            .findFirst()
            .map(h -> new JonosijaTieto(h.getJonosija(), h.getTasasijaJonosija(), h.getTila(), h.getHakemusOid()));

        Valintatapajono jono = jonoWrapper.getValintatapajono();
        if (alinHyvaksyttyJonosija.isPresent()) {
            LOG.info(String.format("Muodostettiin tieto alimmasta hyväksytystä jonolle %s, jossa ei ole varasijatäyttöä: %s",
                jono.getOid(), alinHyvaksyttyJonosija.get()));
            jono.setSivssnovSijoittelunVarasijataytonRajoitus(alinHyvaksyttyJonosija);
        } else {
            LOG.warn(String.format("Ei löytynyt viimeistä hyväksyttyä jonosta %s . Ei voida tallentaa tietoa " +
                "alimmasta hyväksytystä sijasta.", jono.getOid()));
        }

        jonoWrapper.getHakemukset().stream()
                .filter(h -> h.isVaralla() && h.isTilaVoidaanVaihtaa())
                .forEach(h -> asetaTilaksiPeruuntunutAloituspaikatTaynna(h));
    }

}
