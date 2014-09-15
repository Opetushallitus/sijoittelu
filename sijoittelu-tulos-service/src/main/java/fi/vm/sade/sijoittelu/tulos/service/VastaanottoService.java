package fi.vm.sade.sijoittelu.tulos.service;

import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.ILMOITETTU;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.PERUNUT;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.VASTAANOTTANUT;
import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.Vastaanotettavuustila.VASTAANOTETTAVISSA_EHDOLLISESTI;
import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.Vastaanotettavuustila.VASTAANOTETTAVISSA_SITOVASTI;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.LogEntry;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;

@Service
public class VastaanottoService {
    private final ValintatulosDao dao;
    private final RaportointiService raportointiService;

    @Autowired
    public VastaanottoService(final ValintatulosDao dao, final RaportointiService raportointiService) {
        this.dao = dao;
        this.raportointiService = raportointiService;
    }

    public void vastaanota(String hakuOid, String hakemusOid, String hakukohdeOid, ValintatuloksenTila tila, String muokkaaja, String selite) {
        final Optional<SijoitteluAjo> sijoitteluAjo = raportointiService.latestSijoitteluAjoForHaku(hakuOid);
        if (!sijoitteluAjo.isPresent()) {
            throw new IllegalArgumentException("Sijoitteluajoa ei löydy");
        }
        final HakijaDTO hakemus = raportointiService.hakemus(sijoitteluAjo.get(), hakemusOid);
        if (hakemus == null) {
            throw new IllegalArgumentException("Hakemusta ei löydy");
        }
        final List<HakutoiveenYhteenveto> hakutoiveet = YhteenvetoService.hakutoiveidenYhteenveto(hakemus);
        final Optional<HakutoiveenYhteenveto> hakutoive = hakutoiveet.stream().filter(h -> h.hakutoive.getHakukohdeOid().equals(hakukohdeOid)).findFirst();

        if (!hakutoive.isPresent()) {
            throw new IllegalArgumentException("Hakukohdetta ei löydy");
        }

        tarkistaVastaanotettavuus(hakutoive.get(), tila);
        final ValintatulosPerustiedot tiedot = new ValintatulosPerustiedot(hakuOid, hakukohdeOid, hakutoive.get().valintatapajono.getValintatapajonoOid(), hakemusOid, hakemus.getHakijaOid(), hakutoive.get().hakutoive.getHakutoive());
        vastaanota(tiedot, tila, muokkaaja, selite);
    }

    private void tarkistaVastaanotettavuus(HakutoiveenYhteenveto hakutoive, final ValintatuloksenTila tila) {
        if (!Arrays.asList(VASTAANOTTANUT, EHDOLLISESTI_VASTAANOTTANUT, PERUNUT).contains(tila)) {
            throw new IllegalArgumentException("Ei-hyväksytty vastaantottotila: " + tila);
        }
        if (Arrays.asList(VASTAANOTTANUT, PERUNUT).contains(tila) && !Arrays.asList(VASTAANOTETTAVISSA_EHDOLLISESTI, VASTAANOTETTAVISSA_SITOVASTI).contains(hakutoive.vastaanotettavuustila)) {
            throw new IllegalArgumentException();
        }
        if (tila == EHDOLLISESTI_VASTAANOTTANUT && hakutoive.vastaanotettavuustila != VASTAANOTETTAVISSA_EHDOLLISESTI) {
            throw new IllegalArgumentException();
        }
    }

    private void vastaanota(ValintatulosPerustiedot perustiedot, ValintatuloksenTila tila, final String muokkaaja, final String selite) {
        Valintatulos valintatulos = dao.loadValintatulos(perustiedot.hakukohdeOid, perustiedot.valintatapajonoOid, perustiedot.hakemusOid);
        if (valintatulos == null) {
            valintatulos = perustiedot.createValintatulos(tila);
        } else {
            if (valintatulos.getTila() != ILMOITETTU) {
                if (valintatulos.getTila() != EHDOLLISESTI_VASTAANOTTANUT) {
                    throw new IllegalArgumentException("Vastaanotto ei mahdollista tilassa " + valintatulos.getTila());
                } else if (!Arrays.asList(VASTAANOTTANUT, PERUNUT).contains(tila)) {
                    throw new IllegalArgumentException("Tilasta " + valintatulos.getTila() + " ei mahdollista siirtyä tilaan " + tila);
                }
            }
            valintatulos.setTila(tila);
        }
        addLogEntry(valintatulos, muokkaaja, selite);
        dao.createOrUpdateValintatulos(valintatulos);
    }

    private void addLogEntry(final Valintatulos valintatulos, final String muokkaaja, final String selite) {
        LogEntry logEntry = new LogEntry();
        logEntry.setLuotu(new Date());
        logEntry.setMuokkaaja(muokkaaja);
        logEntry.setSelite(selite);
        logEntry.setMuutos(valintatulos.getTila().name());
        valintatulos.getLogEntries().add(logEntry);
    }

    private static class ValintatulosPerustiedot {
        public final String hakuOid;
        public final String hakukohdeOid;
        public final String valintatapajonoOid;
        public final String hakemusOid;
        public final String hakijaOid;
        public final int hakutoiveenPrioriteetti;

        public ValintatulosPerustiedot(final String hakuOid, final String hakukohdeOid, final String valintatapajonoOid, final String hakemusOid, final String hakijaOid, final int hakutoiveenPrioriteetti) {
            this.hakuOid = hakuOid;
            this.hakukohdeOid = hakukohdeOid;
            this.valintatapajonoOid = valintatapajonoOid;
            this.hakemusOid = hakemusOid;
            this.hakijaOid = hakijaOid;
            this.hakutoiveenPrioriteetti = hakutoiveenPrioriteetti;
        }

        public Valintatulos createValintatulos(ValintatuloksenTila tila) {
            final Valintatulos valintatulos = new Valintatulos();
            valintatulos.setHakemusOid(hakemusOid);
            valintatulos.setHakijaOid(hakijaOid);
            valintatulos.setHakukohdeOid(hakukohdeOid);
            valintatulos.setHakuOid(hakuOid);
            valintatulos.setHakutoive(hakutoiveenPrioriteetti);
            valintatulos.setIlmoittautumisTila(IlmoittautumisTila.EI_ILMOITTAUTUNUT);
            valintatulos.setTila(tila);
            valintatulos.setValintatapajonoOid(valintatapajonoOid);
            return valintatulos;
        }
    }

}
