package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public class TilojenMuokkaus {

    public static void asetaTilaksiVaralla(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.VARALLA);
        hakemusWrapper.getHakemus().getTilanKuvaukset().clear();
    }

    public static void asetaTilaksiHyvaksytty(HakemusWrapper hakemus) {
        hakemus.getHakemus().setTila(HakemuksenTila.HYVAKSYTTY);
        hakemus.getHakemus().getTilanKuvaukset().clear();
    }

    public static void asetaTilaksiPeruuntunutToinenJono(HakemusWrapper h) {
        h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHyvaksyttyToisessaJonossa());
    }

    public static void asetaTilaksiPeruuntunutYlempiToive(HakemusWrapper h) {
        h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutYlempiToive());
    }

    public static void asetaTilaksiVarasijaltaHyvaksytty(HakemusWrapper hakemus) {
        hakemus.getHakemus().setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
        hakemus.getHakemus().setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
    }

    public static void asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutEiMahduKasiteltavienVarasijojenMaaraan());
    }

    public static void asetaTilaksiPeruuntunutAloituspaikatTaynna(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutAloituspaikatTaynna());
    }

    public static void asetaTilaksiPeruuntunutHakukierrosPaattynyt(HakemusWrapper hk) {
        hk.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hk.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHakukierrosOnPaattynyt());
    }

    public static Valintatulos muokkaaValintatulos(HakemusWrapper hakemus, HakemusWrapper h, Valintatapajono hyvaksyttyJono, Valintatulos muokattava) {
        Optional<Valintatulos> nykyinenTulos = h.getHenkilo().getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(hyvaksyttyJono.getOid())).findFirst();
        Valintatulos nykyinen;
        if (nykyinenTulos.isPresent()) {
            nykyinen = nykyinenTulos.get();
            nykyinen.setHyvaksyttyVarasijalta(muokattava.getHyvaksyttyVarasijalta(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setIlmoittautumisTila(muokattava.getIlmoittautumisTila(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setJulkaistavissa(muokattava.getJulkaistavissa(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setTila(muokattava.getTila(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
        } else {
            nykyinen = new Valintatulos();
            nykyinen.setHyvaksyttyVarasijalta(muokattava.getHyvaksyttyVarasijalta(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setIlmoittautumisTila(muokattava.getIlmoittautumisTila(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setJulkaistavissa(muokattava.getJulkaistavissa(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setTila(muokattava.getTila(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setValintatapajonoOid(hyvaksyttyJono.getOid(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setHakemusOid(muokattava.getHakemusOid(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setHakijaOid(muokattava.getHakijaOid(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setHakukohdeOid(muokattava.getHakukohdeOid(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setHakuOid(muokattava.getHakuOid(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            nykyinen.setHakutoive(muokattava.getHakutoive(), "Vastaanottotieto peritynyt alemmalta valintatapajonolta");
            hakemus.getHenkilo().getValintatulos().add(nykyinen);
        }
        return nykyinen;
    }

    public static void siirraVastaanottotilaYlemmalleHakutoiveelle(SijoitteluajoWrapper sijoitteluajoWrapper, HakemusWrapper h) {

        Optional<Valintatulos> jononTulos = h.getHenkilo().getValintatulos().stream()
                .filter(v -> v.getValintatapajonoOid().equals(h.getValintatapajono().getValintatapajono().getOid())).findFirst();
        Valintatulos nykyinen;
        if (!jononTulos.isPresent()) {
            nykyinen = new Valintatulos();
            nykyinen.setHyvaksyttyVarasijalta(false, "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
            nykyinen.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
            nykyinen.setJulkaistavissa(false, "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
            nykyinen.setValintatapajonoOid(h.getValintatapajono().getValintatapajono().getOid(), "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
            nykyinen.setHakemusOid(h.getHakemus().getHakemusOid(), "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
            nykyinen.setHakijaOid(h.getHenkilo().getHakijaOid(), "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
            nykyinen.setHakukohdeOid(h.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid(), "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
            nykyinen.setHakuOid(sijoitteluajoWrapper.getSijoitteluajo().getHakuOid(), "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
            nykyinen.setHakutoive(h.getHakemus().getPrioriteetti(), "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
            h.getHenkilo().getValintatulos().add(nykyinen);
        } else {
            nykyinen = jononTulos.get();
        }
        if(h.getHakemus().getPrioriteetti() == 1) {
            nykyinen.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
        } else {
            nykyinen.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
        }
        sijoitteluajoWrapper.getMuuttuneetValintatulokset().add(nykyinen);

    }
}
