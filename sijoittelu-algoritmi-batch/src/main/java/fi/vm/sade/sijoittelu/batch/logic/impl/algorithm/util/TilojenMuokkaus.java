package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.TilankuvauksenTarkenne;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

import java.util.Optional;

public class TilojenMuokkaus {

    public static final String VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_VALINTATAPAJONOLTA = "Vastaanottotieto periytynyt alemmalta valintatapajonolta";
    public static final String VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_HAKUTOIVEELTA = "Vastaanottotieto periytynyt alemmalta hakutoiveelta";

    public static void asetaTilaksiVaralla(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.VARALLA);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA);
    }

    public static void asetaTilaksiHyvaksytty(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.HYVAKSYTTY);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA);
    }

    public static void asetaTilaksiPeruuntunutToinenJono(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_HYVAKSYTTY_TOISESSA_JONOSSA);
    }

    public static void asetaTilaksiPeruuntunutYlempiToive(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_HYVAKSYTTY_YLEMMALLE_HAKUTOIVEELLE);
    }

    public static void asetaTilaksiVarasijaltaHyvaksytty(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.HYVAKSYTTY_VARASIJALTA);
    }

    public static void asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_EI_MAHDU_VARASIJOJEN_MAARAAN);
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
