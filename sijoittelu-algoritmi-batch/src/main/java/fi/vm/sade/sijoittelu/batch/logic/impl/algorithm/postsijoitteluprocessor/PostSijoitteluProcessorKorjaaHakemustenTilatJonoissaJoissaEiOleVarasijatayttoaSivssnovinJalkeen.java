package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuHyvaksyttyihinTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutAloituspaikatTaynna;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYLATTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.PERUUNTUNUT;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Valintatapajono.JonosijaTieto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
                peruunnutaLeikkurisijanAllaOlevatJaJataHyvaksytyiksiNousevatvaralle(jonoWrapper);
                return;
            } else {
                LOG.warn(String.format("Hakukohteen %s jonosta %s ei löytynyt tietoa alimmasta hyväksytystä, vaikka siinä ei ole varasijatäyttöä ja se on " +
                    "sijoiteltu ilman varasijasääntöjä niiden ollessa voimassa. Joko se on sijoiteltu vanhalla sovellusversiolla tai jossain " +
                    "on bugi tai outoa dataa. Peruunnutetaan kaikki varallolijat ja tallennetaan tieto alimmasta hyväksytystä.",
                    jonoWrapper.getHakukohdeWrapper().getHakukohde().getOid(), jono.getOid()));
            }
        }
        peruunnutaKaikkiVarallaolijat(jonoWrapper);
    }

    private void peruunnutaLeikkurisijanAllaOlevatJaJataHyvaksytyiksiNousevatvaralle(ValintatapajonoWrapper jonoWrapper) {
        Valintatapajono jono = jonoWrapper.getValintatapajono();
        JonosijaTieto raja = jono.getSivssnovSijoittelunVarasijataytonRajoitus().get();
        String hakukohdeOid = jonoWrapper.getHakukohdeWrapper().getHakukohde().getOid();
        Assert.isTrue(kuuluuHyvaksyttyihinTiloihin(raja.tila), String.format("Hakukohteen %s jonolla %s ei ole varasijatäyttöä, " +
            "joten sen sivssnov-rajan pitäisi olla hyväksytty-tyyppinen, mutta se on %s", hakukohdeOid, jono.getOid(), raja));

        jonoWrapper.getHakemukset().forEach(hw -> {
            Hakemus h = hw.getHakemus();
            LOG.debug("hakemus: " + h.getHakemusOid() + " (" + h.getTila() + ") jonosija: " + h.getJonosija() + " raja: " + raja.jonosija);
            if (noussutHyvaksyttyjenJoukkoon(h, raja)) {
                LOG.info(String.format("Jätetään tilassa %s ollut hakemus %s (edellinen tila %s, jonosija/tasasijajonosija %s/%s) varalle hakukohteen %s jonossa %s, " +
                    "koska jonossa ei ole varasijatäyttöä, SIVSSNOV on päällä ja leikkurijonosija %s",
                    h.getTila(), h.getHakemusOid(), h.getEdellinenTila(), h.getJonosija(), h.getTasasijaJonosija(), hakukohdeOid, jono.getOid(), raja));
                h.setTila(VARALLA);
            } else if (tulisiHyvaksytyksiMuttaOnSivssnovissaAsetetunRajanAlapuolella(h, raja)) {
                if (hw.isTilaVoidaanVaihtaa()) {
                    LOG.info(String.format("Siirretään tilassa %s ollut hakemus %s peruuntuneeksi hakukohteen %s jonossa %s, jolla ei ole varasijatäyttöä " +
                            "ja jonka tasasijasääntö on %s, koska hakemuksen jonosija on %d ja leikkurijonosija on %s", h.getTila(),
                        h.getHakemusOid(), hakukohdeOid, jono.getOid(), jono.getTasasijasaanto(), h.getJonosija(), raja));
                    asetaTilaksiPeruuntunutAloituspaikatTaynna(h);
                } else {
                    LOG.info(String.format("Tilassa %s ollutta hakemusta %s ei voida siirtää peruuntuneeksi hakukohteen %s jonossa %s, jolla ei ole varasijatäyttöä " +
                            "ja jonka tasasijasääntö on %s, vaikka hakemuksen jonosija on %d ja leikkurijonosija on %s.", h.getTila(),
                        h.getHakemusOid(), hakukohdeOid, jono.getOid(), jono.getTasasijasaanto(), h.getJonosija(), raja));
                }
            } else if (VARALLA.equals(h.getTila())) {
                LOG.info(String.format("Siirretään varalla ollut hakemus %s (jonosija %s) peruuntuneeksi hakukohteen %s jonossa %s, jolla ei ole varasijatäyttöä " +
                        "ja jonka tasasijasääntö on %s ja SIVSSNOV-raja %s.",
                    h.getHakemusOid(), h.getJonosija(), hakukohdeOid, jono.getOid(), jono.getTasasijasaanto(), raja));
                asetaTilaksiPeruuntunutAloituspaikatTaynna(h);
            }
        });
    }

    private boolean noussutHyvaksyttyjenJoukkoon(Hakemus h, JonosijaTieto raja) {
        return Arrays.asList(VARALLA, PERUUNTUNUT, HYLATTY).contains(h.getEdellinenTila()) &&
            kuuluuHyvaksyttyihinTiloihin(h.getTila()) && (h.getJonosija() <= raja.jonosija);
    }

    private boolean tulisiHyvaksytyksiMuttaOnSivssnovissaAsetetunRajanAlapuolella(Hakemus h, JonosijaTieto raja) {
        return h.getJonosija() > raja.jonosija && kuuluuHyvaksyttyihinTiloihin(h.getTila());
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
            .map(h -> new JonosijaTieto(h.getJonosija(), h.getTasasijaJonosija(), h.getTila(), Collections.singletonList(h.getHakemusOid())));

        Valintatapajono jono = jonoWrapper.getValintatapajono();
        String hakukohdeOid = jonoWrapper.getHakukohdeWrapper().getHakukohde().getOid();
        if (alinHyvaksyttyJonosija.isPresent()) {
            LOG.info(String.format("Muodostettiin tieto alimmasta hyväksytystä hakukohteen %s jonolle %s, jossa ei ole varasijatäyttöä: %s",
                hakukohdeOid,
                jono.getOid(), alinHyvaksyttyJonosija.get()));
            jono.setSivssnovSijoittelunVarasijataytonRajoitus(alinHyvaksyttyJonosija);
        } else {
            LOG.warn(String.format("Ei löytynyt viimeistä hyväksyttyä hakukohteen %s jonosta %s . Ei voida tallentaa tietoa " +
                "alimmasta hyväksytystä sijasta.", hakukohdeOid, jono.getOid()));
        }

        jonoWrapper.getHakemukset().stream()
                .filter(h -> h.isVaralla() && h.isTilaVoidaanVaihtaa())
                .forEach(h -> asetaTilaksiPeruuntunutAloituspaikatTaynna(h));
    }

}
