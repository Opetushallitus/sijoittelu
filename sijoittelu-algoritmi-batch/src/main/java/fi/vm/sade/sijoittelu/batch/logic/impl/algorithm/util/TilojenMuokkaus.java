package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.domain.*;

import java.util.Optional;

/**
 * Created by kjsaila on 18/02/15.
 */
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
        if(nykyinenTulos.isPresent()) {
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
}
