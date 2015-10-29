package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public class TilojenMuokkaus {

    public static final String VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_VALINTATAPAJONOLTA = "Vastaanottotieto periytynyt alemmalta valintatapajonolta";
    public static final String VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_HAKUTOIVEELTA = "Vastaanottotieto periytynyt alemmalta hakutoiveelta";

    public static void asetaTilaksiVaralla(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.VARALLA);
        hakemusWrapper.getHakemus().getTilanKuvaukset().clear();
        poistaHakijaryhmastaHyvaksymistiedot(hakemusWrapper.getHakemus());
    }

    public static void asetaTilaksiHyvaksytty(HakemusWrapper hakemus) {
        hakemus.getHakemus().setTila(HakemuksenTila.HYVAKSYTTY);
        hakemus.getHakemus().getTilanKuvaukset().clear();
    }

    public static void asetaTilaksiPeruuntunutToinenJono(HakemusWrapper h) {
        h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHyvaksyttyToisessaJonossa());
        poistaHakijaryhmastaHyvaksymistiedot(h.getHakemus());
    }

    public static void asetaTilaksiPeruuntunutYlempiToive(HakemusWrapper h) {
        h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutYlempiToive());
        poistaHakijaryhmastaHyvaksymistiedot(h.getHakemus());
    }

    public static void asetaTilaksiVarasijaltaHyvaksytty(HakemusWrapper hakemus) {
        hakemus.getHakemus().setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
        hakemus.getHakemus().setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
    }

    public static void asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutEiMahduKasiteltavienVarasijojenMaaraan());
        poistaHakijaryhmastaHyvaksymistiedot(hakemusWrapper.getHakemus());
    }

    public static void asetaTilaksiPeruuntunutAloituspaikatTaynna(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutAloituspaikatTaynna());
        poistaHakijaryhmastaHyvaksymistiedot(hakemusWrapper.getHakemus());
    }

    public static void asetaTilaksiPeruuntunutHakukierrosPaattynyt(HakemusWrapper hk) {
        hk.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hk.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHakukierrosOnPaattynyt());
    }

    public static Optional<Valintatulos> findValintatulos(HakemusWrapper h, String jonoOid) {
        return h.getHenkilo().getValintatulos().stream()
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

    public static void asetaVastaanottanut(SijoitteluajoWrapper sijoitteluajo, HakemusWrapper hakemus) {
        Valintatulos valintatulos = findValintatulos(hakemus, hakemus.getValintatapajono().getValintatapajono().getOid())
                .orElseGet(() -> {
                    Valintatulos v = new Valintatulos(
                            hakemus.getValintatapajono().getValintatapajono().getOid(),
                            hakemus.getHakemus().getHakemusOid(),
                            hakemus.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid(),
                            hakemus.getHenkilo().getHakijaOid(),
                            sijoitteluajo.getSijoitteluajo().getHakuOid(),
                            hakemus.getHakemus().getPrioriteetti()
                    );
                    hakemus.getHenkilo().getValintatulos().add(v);
                    return v;
                });
        valintatulos.setHyvaksyttyVarasijalta(false, VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_HAKUTOIVEELTA);
        valintatulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_HAKUTOIVEELTA);
        valintatulos.setJulkaistavissa(false, VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_HAKUTOIVEELTA);
        if (hakemus.getHakemus().getPrioriteetti() == 1) {
            valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_HAKUTOIVEELTA);
        } else {
            valintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, VASTAANOTTOTIETO_PERIYTYNYT_ALEMMALTA_HAKUTOIVEELTA);
        }
        sijoitteluajo.getMuuttuneetValintatulokset().add(valintatulos);
    }

    private static void poistaHakijaryhmastaHyvaksymistiedot(Hakemus h) {
        if(h != null) {
            h.setHyvaksyttyHakijaryhmasta(false);
            h.setHakijaryhmaOid(null);
        }
    }
}
