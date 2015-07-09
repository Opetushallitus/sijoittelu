package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
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
        poistaVastaanottoTietoKunPeruuntunut(h);
    }

    public static void asetaTilaksiPeruuntunutYlempiToive(HakemusWrapper h) {
        h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutYlempiToive());
        poistaVastaanottoTietoKunPeruuntunut(h);
    }

    public static void asetaTilaksiVarasijaltaHyvaksytty(HakemusWrapper hakemus) {
        hakemus.getHakemus().setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
        hakemus.getHakemus().setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
    }

    public static void asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutEiMahduKasiteltavienVarasijojenMaaraan());
        poistaVastaanottoTietoKunPeruuntunut(hakemusWrapper);
    }

    public static void asetaTilaksiPeruuntunutAloituspaikatTaynna(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hakemusWrapper.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutAloituspaikatTaynna());
        poistaVastaanottoTietoKunPeruuntunut(hakemusWrapper);
    }

    public static void asetaTilaksiPeruuntunutHakukierrosPaattynyt(HakemusWrapper hk) {
        hk.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        hk.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHakukierrosOnPaattynyt());
        poistaVastaanottoTietoKunPeruuntunut(hk);
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

    private static void poistaVastaanottoTietoKunPeruuntunut(HakemusWrapper h) {
        Optional<Valintatulos> valintaTulosOpt = h.getHenkilo().getValintatulos().stream()
                .filter(v -> v.getValintatapajonoOid().equals(h.getValintatapajono().getValintatapajono().getOid()))
                .findFirst();

        if(valintaTulosOpt.isPresent()) {
            Valintatulos valintatulos = valintaTulosOpt.get();
            if(!ValintatuloksenTila.KESKEN.equals(valintatulos.getTila())) {
                valintatulos.setTila(ValintatuloksenTila.KESKEN);
                valintatulos.getLogEntries().add(createLogEntry(ValintatuloksenTila.KESKEN, "Poistettu vastaanottotieto koska peruuntunut"));
            }
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
