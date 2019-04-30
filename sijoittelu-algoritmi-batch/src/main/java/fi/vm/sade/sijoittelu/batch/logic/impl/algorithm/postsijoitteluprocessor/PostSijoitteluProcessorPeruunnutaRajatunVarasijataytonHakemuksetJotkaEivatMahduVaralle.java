package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutAloituspaikatTaynna;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Optional;

/**
 * Tämä prosessori on ajettava ennen <code>PostSijoitteluProcessorAsetaSivssnov</code> :ia, jotta tämä voi tunnistaa
 * tilanteen, jossa ollaan ajamassa ensimmäistä sijoittelua sen jälkeen kun varasijasäännöt ovat astuneet voimaan.
 *
 * @see PostSijoitteluProcessorAsetaSivssnov
 */
public class PostSijoitteluProcessorPeruunnutaRajatunVarasijataytonHakemuksetJotkaEivatMahduVaralle implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorPeruunnutaRajatunVarasijataytonHakemuksetJotkaEivatMahduVaralle.class);
    private static final boolean ENABLE_VTKU_31 = false;

    @Override
    public void process(SijoitteluajoWrapper ajoWrapper) {
        LOG.debug("Haun " + ajoWrapper.getSijoitteluajo().getHakuOid() + " varasijasäännöt voimassa: " +
            ajoWrapper.getVarasijaSaannotAstuvatVoimaan() + " -- " + ajoWrapper.getVarasijaTayttoPaattyy());
        if (!ajoWrapper.varasijaSaannotVoimassa()) {
            LOG.debug("Haun " + ajoWrapper.getSijoitteluajo().getHakuOid() +
                " varasijasäännöt eivät ole voimassa, joten ei tehdä mitään.");
            return;
        }

        ajoWrapper.getHakukohteet().forEach(h -> h.getValintatapajonot().forEach(j -> process(j)));
    }

    private void process(ValintatapajonoWrapper jonoWrapper) {
        Valintatapajono jono = jonoWrapper.getValintatapajono();
        if (jono.vapaaVarasijataytto()) {
            return;
        }
        SijoitteluajoWrapper sijoitteluajoWrapper = jonoWrapper.getHakukohdeWrapper().getSijoitteluajoWrapper();
        assertSijoiteltuEnnenVarasijataytonLoppumista(jonoWrapper, sijoitteluajoWrapper);
        
        if (Boolean.TRUE.equals(jono.getEiVarasijatayttoa())) {
            if(sijoitteluajoWrapper.isKKHaku()) {
                jonoWrapper.getHakemukset().stream()
                        .filter(h -> h.isVaralla() && h.isTilaVoidaanVaihtaa())
                        .forEach(h -> asetaTilaksiPeruuntunutAloituspaikatTaynna(h));
            }
            return;
        }

        if (jono.rajoitettuVarasijaTaytto()) {
            if (jono.getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()) {
                final int jonosijaJonkaAllaOlevatPeruunnutetaan = paatteleJonosijaJonkaAllaOlevatPeruunnutetaan(jonoWrapper);

                jonoWrapper.getHakemukset().stream()
                    .filter(HakemusWrapper::isVaralla)
                    .filter(h -> h.getHakemus().getJonosija() > jonosijaJonkaAllaOlevatPeruunnutetaan)
                    .forEach(h -> asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(h));

                return;
            } else {
                int varasijat = jono.getVarasijat();
                jonoWrapper.getHakemukset().stream()
                        .filter(HakemusWrapper::isVaralla)
                        .sorted(Comparator.comparing(h -> h.getHakemus().getJonosija()))
                        .skip(varasijat)
                        .forEach(h -> asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(h));
                return;
            }
        }

        throw new IllegalStateException("Jonolla " + jono.getOid() + " piti olla ei varasijatäyttöä tai rajoitettu varasijatäyttö. Vaikuttaa bugilta.");
    }

    private int paatteleJonosijaJonkaAllaOlevatPeruunnutetaan(ValintatapajonoWrapper jonoJollaRajoitettuVarasijaTaytto) {
        Optional<Hakemus> viimeinenEdellisessaSijoittelussaVarallaOllutHakemus = jonoJollaRajoitettuVarasijaTaytto.getHakemukset().stream()
            .filter(h -> h.getHakemus().getEdellinenTila() == HakemuksenTila.VARALLA)
            .filter(h -> h.getHakemus().getTila() != HakemuksenTila.HYLATTY)
            .sorted(Comparator.comparing(h -> ((HakemusWrapper) h).getHakemus().getJonosija()).reversed())
            .map(HakemusWrapper::getHakemus)
            .findFirst();

        Valintatapajono jono = jonoJollaRajoitettuVarasijaTaytto.getValintatapajono();

        Optional<Integer> sivssnovSijoittelunViimeistenVarallaolijoidenJonosija =
            jono.getSivssnovSijoittelunViimeistenVarallaolijoidenJonosija().map(j -> j.jonosija);

        int viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija = viimeinenEdellisessaSijoittelussaVarallaOllutHakemus
            .map(Hakemus::getJonosija)
            .orElse(0);

        if (viimeinenEdellisessaSijoittelussaVarallaOllutHakemus.isPresent() && sivssnovSijoittelunViimeistenVarallaolijoidenJonosija.isEmpty()) {
            LOG.warn(String.format("Jonolla %s on rajoitettu varasijatäyttö ja sen edellisessä sijoittelussa " +
                    "viimeinen varasijalla ollut hakemus %s on ollut jonosijalla %d, mutta jonon tietoihin ei ole tallennettu tietoa sivssnov-" +
                    "sijoittelussa viimeisenä varalla olleen hakemuksen jonosijasta. Joko jono on sijoiteltu vanhalla sovellusversiolla tai tämä on bugi.",
                jono.getOid(), viimeinenEdellisessaSijoittelussaVarallaOllutHakemus.get().getHakemusOid(),
                viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija));
            return viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija;
        }

        if (viimeinenEdellisessaSijoittelussaVarallaOllutHakemus.isPresent() && sivssnovSijoittelunViimeistenVarallaolijoidenJonosija.isPresent()) {
            if (viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija != sivssnovSijoittelunViimeistenVarallaolijoidenJonosija.get()) {
                LOG.info(String.format("Jonolla %s on rajoitettu varasijatäyttö ja sen edellisessä sijoittelussa " +
                        "viimeinen varasijalla ollut hakemus %s on ollut jonosijalla %d, mutta jonon tietoihin tallennettu tieto sivssnov-" +
                        "sijoittelussa viimeisenä varalla olleen hakemuksen jonosijasta on %s. Ilmeisesti pisteet ovat muuttuneet tms.",
                    jono.getOid(), viimeinenEdellisessaSijoittelussaVarallaOllutHakemus.get().getHakemusOid(),
                    viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija, sivssnovSijoittelunViimeistenVarallaolijoidenJonosija.get()));
            }
        }
        if (ENABLE_VTKU_31) {
            LOG.info("Käytetään VTKU-31:n uutta logiikkaa ja rajoitetaan varasijoja SIVSSNOV-sijoittelussa tallennetun tiedon perusteella.");
            return sivssnovSijoittelunViimeistenVarallaolijoidenJonosija.get();
        } else {
            LOG.info("Ei vielä käytetä VTKU-31:n uutta logiikkaa vaan rajoitetaan varasijoja edellisten tilojen perusteella.");
            return viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija;
        }
    }

    private void assertSijoiteltuEnnenVarasijataytonLoppumista(ValintatapajonoWrapper jonoWrapper, SijoitteluajoWrapper sijoitteluajoWrapper) {
        Valintatapajono jono = jonoWrapper.getValintatapajono();
        if (!jono.getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()
            && sijoitteluajoWrapper.onkoVarasijaSaannotVoimassaJaVarasijaTayttoPaattynyt(jonoWrapper)) {
                throw new IllegalStateException("Haun " + sijoitteluajoWrapper.getSijoitteluajo().getHakuOid() + " hakukohteen " +
                    jonoWrapper.getHakukohdeWrapper().getHakukohde().getOid() + " valintatapajonoa " + jono.getOid() +
                    " ei ole kertaakaan sijoiteltu ilman varasijasääntöjä niiden ollessa voimassa, vaikka sen varasijatäyttö " +
                    "on jo päättynyt.");
        }
    }
}
