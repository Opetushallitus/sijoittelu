package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;

import java.util.Date;
import java.util.Optional;

public class TilojenMuokkaus {

    public static void asetaTilaksiVaralla(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.VARALLA);
        hakemusWrapper.getHakemus().getTilanKuvaukset().clear();
    }

    public static void asetaTilaksiHyvaksytty(HakemusWrapper hakemus) {
        hakemus.getHakemus().setTila(HakemuksenTila.HYVAKSYTTY);
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
        muokattava.setTila(ValintatuloksenTila.KESKEN);
        muokattava.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY);
        muokattava.setHyvaksyttyVarasijalta(false);
        return nykyinen;
    }

    public static void poistaVastaanottoTietoKunPeruuntunut(HakemusWrapper hakemus, SijoitteluajoWrapper sijoitteluAjo) {
        Optional<Valintatulos> valintatulosOptional = hakemus.getHenkilo().getValintatulos().stream().filter(vt -> vt.getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())).findFirst();
        if(valintatulosOptional.isPresent()) {
            Valintatulos valintatulos = valintatulosOptional.get();
            poistaVastaanottoTietoKunPeruuntunut(valintatulos);
            sijoitteluAjo.getMuuttuneetValintatulokset().add(valintatulos);
        }
    }

    public static void poistaVastaanottoTietoKunPeruuntunut(Valintatulos valintatulos) {

            if(!ValintatuloksenTila.KESKEN.equals(valintatulos.getTila())) {
                valintatulos.setTila(ValintatuloksenTila.KESKEN);
                valintatulos.getLogEntries().add(createLogEntry(ValintatuloksenTila.KESKEN, "Poistettu vastaanottotieto koska peruuntunut"));
            }
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
