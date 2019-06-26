package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Valintatapajono.JonosijaTieto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Tämä prosessori on ajettava ennen <code>PostSijoitteluProcessorAsetaSivssnov</code> :ia, jotta tämä voi tunnistaa
 * tilanteen, jossa ollaan ajamassa ensimmäistä sijoittelua sen jälkeen kun varasijasäännöt ovat astuneet voimaan.
 *
 * @see PostSijoitteluProcessorAsetaSivssnov
 */
public class PostSijoitteluProcessorPeruunnutaRajatunVarasijataytonHakemuksetJotkaEivatMahduVaralle implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorPeruunnutaRajatunVarasijataytonHakemuksetJotkaEivatMahduVaralle.class);

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

        if (jono.rajoitettuVarasijaTaytto()) {
            if (jono.getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()) {
                peruunnutaRajatunVarasijataytonJonossaSivssnovSijoittelunJalkeen(jonoWrapper);
            } else {
                peruunnutaRajatunVarasijataytonJonossaEnnenSivssnovia(jonoWrapper, jono);
            }
        }
    }

    private void peruunnutaRajatunVarasijataytonJonossaEnnenSivssnovia(ValintatapajonoWrapper jonoWrapper, Valintatapajono jono) {
        List<HakemusWrapper> varallaOlijatEnnenRajoittamista = jonoWrapper.getHakemukset().stream()
            .filter(HakemusWrapper::isVaralla)
            .sorted(new HakemusWrapperComparator())
            .collect(Collectors.toList());

        Optional<Integer> viimeinenJonosijaJokaMahtuuVaralle = paatteleViimeinenJonosijaJokaMahtuuVaralle(jonoWrapper, varallaOlijatEnnenRajoittamista);

        if (viimeinenJonosijaJokaMahtuuVaralle.isPresent()) {
            List<Hakemus> viimeisenVarallaolosijanHakemukset = varallaOlijatEnnenRajoittamista.stream()
                .map(HakemusWrapper::getHakemus)
                .filter(h -> h.getJonosija().equals(viimeinenJonosijaJokaMahtuuVaralle.get()))
                .collect(Collectors.toList());
            JonosijaTieto viimeistenVarallaolijoidenSija = new JonosijaTieto(viimeisenVarallaolosijanHakemukset);
            LOG.info(String.format("Muodostettiin tieto viimeisestä varasijasta jonolle %s, jossa on %d varasijaa: %s",
                jono.getOid(), jono.getVarasijat(), viimeistenVarallaolijoidenSija));
            jono.setSivssnovSijoittelunVarasijataytonRajoitus(Optional.of(viimeistenVarallaolijoidenSija));

            varallaOlijatEnnenRajoittamista.forEach(h -> {
                if (h.getHakemus().getJonosija() > viimeinenJonosijaJokaMahtuuVaralle.get()) {
                    asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(h);
                }
            });
        } else {
            LOG.warn(String.format("Ei löytynyt viimeistä varallaolijaa hakukohteen %s jonosta %s . Ei voida peruunnuttaa ketään " +
                "eikä tallentaa tietoa viimeisistä varallaolijoista.", jonoWrapper.getHakukohdeWrapper().getHakukohde().getOid(), jono.getOid()));
        }
    }

    private Optional<Integer> paatteleViimeinenJonosijaJokaMahtuuVaralle(ValintatapajonoWrapper valintatapajonoWrapper, List<HakemusWrapper> varallaOlijatEnnenRajoittamista) {
        Valintatapajono jono = valintatapajonoWrapper.getValintatapajono();
        int varasijat = jono.getVarasijat();
        int varallaolijoidenMaara = varallaOlijatEnnenRajoittamista.size();
        if (varallaolijoidenMaara < varasijat) {
            LOG.warn(String.format("Hakukohteen %s jonossa %s on vain %d hakemusta varalla ja varasijojen määrä on %d, " +
                "joten %d varasijaa jää käyttämättä.", valintatapajonoWrapper.getHakukohdeWrapper().getHakukohde().getOid(), jono.getOid(),
                varallaolijoidenMaara, varasijat, (varasijat - varallaolijoidenMaara)));
            return varallaOlijatEnnenRajoittamista.stream()
                .reduce((first, second) -> second)
                .map(hw -> hw.getHakemus().getJonosija());
        } else {
            return varallaOlijatEnnenRajoittamista.stream()
                .skip(varasijat - 1)
                .map(hw -> hw.getHakemus().getJonosija())
                .findFirst();
        }
    }

    private void peruunnutaRajatunVarasijataytonJonossaSivssnovSijoittelunJalkeen(ValintatapajonoWrapper jonoWrapper) {
        ViimeinenJonosijaJokaMahtuuVaralle viimeinenJonosijaJokaMahtuuVaralle =
            paatteleJonosijaJonkaAllaOlevatPeruunnutetaanVarasijasaantojenOllessaVoimassa(jonoWrapper);
        final int jonosijaJonkaAllaOlevatPeruunnutetaan = viimeinenJonosijaJokaMahtuuVaralle.jonosija;

        jonoWrapper.getHakemukset().stream()
            .filter(HakemusWrapper::isVaralla)
            .filter(h -> h.getHakemus().getJonosija() > jonosijaJonkaAllaOlevatPeruunnutetaan)
            .forEach(h -> asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(h));
    }

    private ViimeinenJonosijaJokaMahtuuVaralle paatteleJonosijaJonkaAllaOlevatPeruunnutetaanVarasijasaantojenOllessaVoimassa(ValintatapajonoWrapper jonoJollaRajoitettuVarasijaTaytto) {
        Optional<Hakemus> viimeinenEdellisessaSijoittelussaVarallaOllutHakemus = jonoJollaRajoitettuVarasijaTaytto.getHakemukset().stream()
            .filter(h -> h.getHakemus().getEdellinenTila() == HakemuksenTila.VARALLA)
            .filter(h -> h.getHakemus().getTila() != HakemuksenTila.HYLATTY)
            .sorted(Comparator.comparing(h -> ((HakemusWrapper) h).getHakemus().getJonosija()).reversed())
            .map(HakemusWrapper::getHakemus)
            .findFirst();

        Valintatapajono jono = jonoJollaRajoitettuVarasijaTaytto.getValintatapajono();
        assertCorrectLimitType(jono);
        return valitseRajaJonosija(jonoJollaRajoitettuVarasijaTaytto, viimeinenEdellisessaSijoittelussaVarallaOllutHakemus);
    }

    private ViimeinenJonosijaJokaMahtuuVaralle valitseRajaJonosija(ValintatapajonoWrapper jonoJollaRajoitettuVarasijaTaytto,
                                                                   Optional<Hakemus> viimeinenEdellisessaSijoittelussaVarallaOllutHakemus) {
        Valintatapajono jono = jonoJollaRajoitettuVarasijaTaytto.getValintatapajono();
        String hakukohdeOid = jonoJollaRajoitettuVarasijaTaytto.getHakukohdeWrapper().getHakukohde().getOid();
        Optional<Integer> sivssnovSijoittelunTallennettuRaja = jono.getSivssnovSijoittelunVarasijataytonRajoitus().map(j -> j.jonosija);

        int viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija = viimeinenEdellisessaSijoittelussaVarallaOllutHakemus
            .map(Hakemus::getJonosija)
            .orElse(0);

        if (viimeinenEdellisessaSijoittelussaVarallaOllutHakemus.isPresent() && sivssnovSijoittelunTallennettuRaja.isEmpty()) {
            return new ViimeinenJonosijaJokaMahtuuVaralle(
                String.format("Hakukohteen %s jonolla %s on rajoitettu varasijatäyttö ja sen edellisessä sijoittelussa " +
                        "viimeinen varasijalla ollut hakemus %s on ollut jonosijalla %d, mutta jonon tietoihin ei ole tallennettu tietoa sivssnov-" +
                        "sijoittelussa viimeisenä varalla olleen hakemuksen jonosijasta. Joko jono on sijoiteltu vanhalla sovellusversiolla tai tämä on bugi.",
                    hakukohdeOid, jono.getOid(), viimeinenEdellisessaSijoittelussaVarallaOllutHakemus.get().getHakemusOid(),
                    viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija),
                LOG::warn,
                viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija);
        }

        if (viimeinenEdellisessaSijoittelussaVarallaOllutHakemus.isPresent() && sivssnovSijoittelunTallennettuRaja.isPresent()) {
            if (viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija != sivssnovSijoittelunTallennettuRaja.get()) {
                LOG.info(String.format("Hakukohteen %s jonolla %s on rajoitettu varasijatäyttö ja sen edellisessä sijoittelussa " +
                        "viimeinen varasijalla ollut hakemus %s on ollut jonosijalla %d, mutta jonon tietoihin tallennettu tieto sivssnov-" +
                        "sijoittelussa viimeisenä varalla olleen hakemuksen jonosijasta on %s. Ilmeisesti pisteet ovat muuttuneet tms.",
                    hakukohdeOid, jono.getOid(), viimeinenEdellisessaSijoittelussaVarallaOllutHakemus.get().getHakemusOid(),
                    viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija, sivssnovSijoittelunTallennettuRaja.get()));
            }
        }

        if (!sivssnovSijoittelunTallennettuRaja.isPresent()) {
            return new ViimeinenJonosijaJokaMahtuuVaralle(String.format("Hakukohteen %s jonolla %s on rajoitettu varasijatäyttö, mutta siltä ei löytynyt edellisessä sijoittelussa alinta varalla ollutta " +
                "hakemusta eikä SIVSSNOV-sijoittelussa tallennettua tietoa alimmasta varallaolijasta. Ei voida siis peruunnuttaa ketään. " +
                "Vaikuttaa bugilta tai oudolta datata.", hakukohdeOid, jono.getOid()),
                LOG::warn,
                Integer.MAX_VALUE);
        }

        if (jonoJollaRajoitettuVarasijaTaytto.getSijoitteluConfiguration().kaytaVtku31SaantoaRajoitetussaVarasijataytossa) {
            Integer raja = sivssnovSijoittelunTallennettuRaja.get();
            return new ViimeinenJonosijaJokaMahtuuVaralle("Käytetään VTKU-31:n uutta logiikkaa ja rajoitetaan varasijoja SIVSSNOV-sijoittelussa tallennetun tiedon perusteella (" +
                raja + ")",
                LOG::info,
                sivssnovSijoittelunTallennettuRaja.get());
        } else {
            return new ViimeinenJonosijaJokaMahtuuVaralle("Ei vielä käytetä VTKU-31:n uutta logiikkaa vaan rajoitetaan varasijoja edellisten tilojen perusteella (" +
                viimeinenEdellisessaSijoittelussaVarallaOllutHakemus + ")",
                LOG::info,
                viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija);
        }
    }


    private void assertCorrectLimitType(Valintatapajono jono) {
        jono.getSivssnovSijoittelunVarasijataytonRajoitus().ifPresent(j -> {
            Assert.isTrue(VARALLA.equals(j.tila), String.format("Jonolla %s on rajattu varasijatäyttö (%d), " +
                "joten sen sivssnov-rajan pitäisi olla varalla-tyyppinen, mutta se on %s", jono.getOid(), jono.getVarasijat(), j));
        });
    }

    private class ViimeinenJonosijaJokaMahtuuVaralle {
        private final int jonosija;

        /**
         * @param doLog  a workaround for the fact that slf4j does not support passing log level as a parameter.
         */
        private ViimeinenJonosijaJokaMahtuuVaralle(String logMessage, Consumer<String> doLog, int jonosija) {
            doLog.accept(logMessage);
            this.jonosija = jonosija;
        }
    }
}
