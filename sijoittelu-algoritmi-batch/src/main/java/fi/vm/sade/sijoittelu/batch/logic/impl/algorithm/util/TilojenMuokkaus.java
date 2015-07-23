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
            nykyinen.setHyvaksyttyVarasijalta(muokattava.getHyvaksyttyVarasijalta());
            nykyinen.setIlmoittautumisTila(muokattava.getIlmoittautumisTila());
            nykyinen.setJulkaistavissa(muokattava.getJulkaistavissa());
            nykyinen.setLogEntries(muokattava.getLogEntries());
            nykyinen.setTila(muokattava.getTila());
        } else {
            nykyinen = new Valintatulos();
            nykyinen.setHyvaksyttyVarasijalta(muokattava.getHyvaksyttyVarasijalta());
            nykyinen.setIlmoittautumisTila(muokattava.getIlmoittautumisTila());
            nykyinen.setJulkaistavissa(muokattava.getJulkaistavissa());
            nykyinen.setLogEntries(muokattava.getLogEntries());
            nykyinen.setTila(muokattava.getTila());
            nykyinen.setValintatapajonoOid(hyvaksyttyJono.getOid());
            nykyinen.setHakemusOid(muokattava.getHakemusOid());
            nykyinen.setHakijaOid(muokattava.getHakijaOid());
            nykyinen.setHakukohdeOid(muokattava.getHakukohdeOid());
            nykyinen.setHakuOid(muokattava.getHakuOid());
            nykyinen.setHakutoive(muokattava.getHakutoive());
            hakemus.getHenkilo().getValintatulos().add(nykyinen);
        }
        return nykyinen;
    }

    public static void siirraVastaanottotilaYlemmalleHakutoiveelle(SijoitteluajoWrapper sijoitteluajoWrapper, HakemusWrapper h) {

        Optional<Valintatulos> jononTulos = h.getHenkilo().getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(h.getValintatapajono().getValintatapajono().getOid())).findFirst();
        Valintatulos nykyinen;
        if (!jononTulos.isPresent()) {
            nykyinen = new Valintatulos();
            nykyinen.setHyvaksyttyVarasijalta(false);
            nykyinen.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY);
            nykyinen.setJulkaistavissa(false);
            nykyinen.setValintatapajonoOid(h.getValintatapajono().getValintatapajono().getOid());
            nykyinen.setHakemusOid(h.getHakemus().getHakemusOid());
            nykyinen.setHakijaOid(h.getHenkilo().getHakijaOid());
            nykyinen.setHakukohdeOid(h.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid());
            nykyinen.setHakuOid(Optional.ofNullable(sijoitteluajoWrapper.getSijoitteluajo()).orElse(new SijoitteluAjo()).getHakuOid());
            nykyinen.setHakutoive(h.getHakemus().getPrioriteetti());
            h.getHenkilo().getValintatulos().add(nykyinen);
        } else {
            nykyinen = jononTulos.get();
        }
        if(h.getHakemus().getPrioriteetti() == 1) {
            nykyinen.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        } else {
            nykyinen.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
        }
        nykyinen.getLogEntries().add(createLogEntry(nykyinen.getTila(), "Vastaanottotieto peritynyt alemmalta hakutoiveelta"));
        sijoitteluajoWrapper.getMuuttuneetValintatulokset().add(nykyinen);

    }

    private static LogEntry createLogEntry(ValintatuloksenTila tila, String selite) {
        LogEntry logEntry = new LogEntry();
        logEntry.setLuotu(new Date());
        logEntry.setMuokkaaja("sijoittelu");
        logEntry.setSelite(selite);
        if (tila == null) {
            logEntry.setMuutos("");
        } else {
            logEntry.setMuutos(tila.name());
        }
        return logEntry;
    }
}
