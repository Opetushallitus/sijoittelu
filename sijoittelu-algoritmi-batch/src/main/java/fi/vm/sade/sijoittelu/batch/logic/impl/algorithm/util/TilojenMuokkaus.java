package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakijaryhma;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.TilanKuvaukset;
import fi.vm.sade.sijoittelu.domain.TilankuvauksenTarkenne;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

import java.util.Map;
import java.util.Optional;

public class TilojenMuokkaus {

    public static final String VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_VALINTATAPAJONOLTA = "Vastaanottotieto periytynyt alemmalta valintatapajonolta";
    public static final String VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_HAKUTOIVEELTA = "Vastaanottotieto periytynyt alemmalta hakutoiveelta";

    public static void asetaTilaksiHylatty(Hakemus hakemus, Map<String, String> tilanKuvaukset) {
        hakemus.setTila(HakemuksenTila.HYLATTY);
        hakemus.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA);
        hakemus.setTilanKuvaukset(tilanKuvaukset);
    }

    public static void asetaTilaksiHylattyHakijaryhmaanKuulumattomana(HakemusWrapper hakemusWrapper, Hakijaryhma hakijaryhma) {
        asetaTilaksiHylattyHakijaryhmaanKuulumattomana(hakemusWrapper.getHakemus(), hakijaryhma);
    }

    public static void asetaTilaksiHylattyHakijaryhmaanKuulumattomana(Hakemus hakemus, Hakijaryhma hakijaryhma) {
        hakemus.setTila(HakemuksenTila.HYLATTY);
        hakemus.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.HYLATTY_HAKIJARYHMAAN_KUULUMATTOMANA);
        hakemus.setTilanKuvaukset(TilanKuvaukset.hylattyHakijaryhmaanKuulumattomana(hakijaryhma.getNimi()));
    }

    public static void asetaTilaksiVaralla(HakemusWrapper hakemusWrapper) {
        asetaTilaksiVaralla(hakemusWrapper.getHakemus());
    }

    public static void asetaTilaksiVaralla(Hakemus hakemus) {
        hakemus.setTila(HakemuksenTila.VARALLA);
        hakemus.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA);
    }

    public static void asetaTilaksiHyvaksytty(HakemusWrapper hakemusWrapper) {
        asetaTilaksiHyvaksytty(hakemusWrapper.getHakemus());
    }

    public static void asetaTilaksiHyvaksytty(Hakemus hakemus) {
        hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
        hakemus.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA);
    }

    public static void asetaTilaksiPeruuntunutToinenJono(HakemusWrapper hakemusWrapper) {
        asetaTilaksiPeruuntunutToinenJono(hakemusWrapper.getHakemus());
    }

    public static void asetaTilaksiPeruuntunutToinenJono(Hakemus hakemus) {
        hakemus.setTila(HakemuksenTila.PERUUNTUNUT);
        hakemus.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_HYVAKSYTTY_TOISESSA_JONOSSA);
    }

    public static void asetaTilaksiPeruuntunutYlempiToive(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_HYVAKSYTTY_YLEMMALLE_HAKUTOIVEELLE);
    }

    public static void asetaTilaksiPeruuntunutAlempiToive(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_HYVAKSYTTY_ALEMMALLE_HAKUTOIVEELLE);
    }

    public static void asetaTilaksiVarasijaltaHyvaksytty(HakemusWrapper hakemusWrapper) {
        asetaTilaksiVarasijaltaHyvaksytty(hakemusWrapper.getHakemus());
    }

    public static void asetaTilaksiVarasijaltaHyvaksytty(Hakemus hakemus) {
        hakemus.setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
        hakemus.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.HYVAKSYTTY_VARASIJALTA);
    }

    public static void asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(HakemusWrapper hakemusWrapper) {
        asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(hakemusWrapper.getHakemus());
    }

    public static void asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(Hakemus hakemus) {
        hakemus.setTila(HakemuksenTila.PERUUNTUNUT);
        hakemus.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_EI_MAHDU_VARASIJOJEN_MAARAAN);
    }

    public static void asetaTilaksiPeruuntunutAloituspaikatTaynna(HakemusWrapper hakemusWrapper) {
        asetaTilaksiPeruuntunutAloituspaikatTaynna(hakemusWrapper.getHakemus());
    }

    public static void asetaTilaksiPeruuntunutAloituspaikatTaynna(Hakemus hakemus) {
        hakemus.setTila(HakemuksenTila.PERUUNTUNUT);
        hakemus.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_ALOITUSPAIKAT_TAYNNA);
    }

    public static void asetaTilaksiPeruuntunutHakukierrosPaattynyt(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_HAKUKIERROS_PAATTYNYT);
    }

    public static void asetaTilaksiPeruuntunutVastaanottanutToisenPaikan(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN);
    }

    public static void asetaTilaksiPeruuntunutVastaanottanutToisenPaikanYhdenPaikanSaannonPiirissa(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN_YHDEN_SAANNON_PAIKAN_PIIRISSA);
    }

    public static void asetaTilaksiPerunut(HakemusWrapper hakemusWrapper) {
        asetaTilaksiPerunut(hakemusWrapper.getHakemus());
    }

    public static void asetaTilaksiPerunut(Hakemus hakemus) {
        hakemus.setTila(HakemuksenTila.PERUNUT);
        hakemus.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA);
    }

    public static void asetaTilaksiPerunutEiVastaanottanutMaaraaikana(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUNUT);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_EI_VASTAANOTTANUT_MAARAAIKANA);
    }

    public static void asetaTilaksiPeruutettu(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUTETTU);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA);
    }

    public static void periTila(HakemusWrapper perija, HakemusWrapper perittava) {
        perija.getHakemus().setTila(perittava.getHakemus().getTila());
        perija.getHakemus().setTilankuvauksenTarkenne(perittava.getHakemus().getTilankuvauksenTarkenne());
        perija.getHakemus().setTilanKuvaukset(perittava.getHakemus().getTilanKuvaukset());
    }

    public static Optional<Valintatulos> findValintatulos(HakemusWrapper hakemusWrapper, String jonoOid) {
        return hakemusWrapper.getHenkilo().getValintatulos().stream()
                .filter(v -> jonoOid.equals(v.getValintatapajonoOid()))
                .findAny();
    }

    public static Valintatulos siirraValintatulosHyvaksyttyynJonoon(HakemusWrapper hyvaksyttyHakemus,
                                                                    HakemusWrapper vanhaHakemus,
                                                                    Valintatapajono hyvaksyttyJono,
                                                                    Valintatulos vanhaTulos) {
        Valintatulos hyvaksyttyTulos = findValintatulos(vanhaHakemus, hyvaksyttyJono.getOid())
                .orElseGet(() -> {
                    Valintatulos v = new Valintatulos(
                            hyvaksyttyJono.getOid(),
                            vanhaTulos.getHakemusOid(),
                            vanhaTulos.getHakukohdeOid(),
                            vanhaTulos.getHakijaOid(),
                            vanhaTulos.getHakuOid(),
                            vanhaTulos.getHakutoive()
                    );
                    hyvaksyttyHakemus.getHenkilo().getValintatulos().add(v);
                    return v;
                });
        hyvaksyttyTulos.setHyvaksyttyVarasijalta(vanhaTulos.getHyvaksyttyVarasijalta(), VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_VALINTATAPAJONOLTA);
        hyvaksyttyTulos.setIlmoittautumisTila(vanhaTulos.getIlmoittautumisTila(), VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_VALINTATAPAJONOLTA);
        hyvaksyttyTulos.setJulkaistavissa(vanhaTulos.getJulkaistavissa(), VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_VALINTATAPAJONOLTA);
        hyvaksyttyTulos.setTila(vanhaTulos.getTila(), VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_VALINTATAPAJONOLTA);
        return hyvaksyttyTulos;
    }

    public static void asetaVastaanottanut(SijoitteluajoWrapper sijoitteluajo, HakemusWrapper hakemusWrapper) {
        Valintatulos valintatulos = findValintatulos(hakemusWrapper, hakemusWrapper.getValintatapajono().getValintatapajono().getOid())
                .orElseGet(() -> {
                    Valintatulos v = new Valintatulos(
                            hakemusWrapper.getValintatapajono().getValintatapajono().getOid(),
                            hakemusWrapper.getHakemus().getHakemusOid(),
                            hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid(),
                            hakemusWrapper.getHenkilo().getHakijaOid(),
                            sijoitteluajo.getSijoitteluajo().getHakuOid(),
                            hakemusWrapper.getHakemus().getPrioriteetti()
                    );
                    hakemusWrapper.getHenkilo().getValintatulos().add(v);
                    return v;
                });
        valintatulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_HAKUTOIVEELTA);
        // TODO: Peritymisen yhteydessä muutetaan sitovaksi jos ylin hakutoive jolle hakija voi vielä tulla hyväksytyksi.
        if (hakemusWrapper.getHakemus().getPrioriteetti() == 1) {
            valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_HAKUTOIVEELTA);
        } else {
            valintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_HAKUTOIVEELTA);
        }
        sijoitteluajo.addMuuttuneetValintatulokset(valintatulos);
    }
}
