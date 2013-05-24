package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactory;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.laskenta.dao.Dao;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Kari Kammonen
 */
@Service
public class SijoitteluBusinessServiceImpl implements SijoitteluBusinessService {

    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluBusinessServiceImpl.class);


    @Autowired
    private SijoitteluAlgorithmFactory algorithmFactory;

    @Autowired
    private Dao dao;

    public void sijoittele() {

    }

    @Override
    public void sijoittele(SijoitteleTyyppi sijoitteluTyyppi) {

        String hakuOid = sijoitteluTyyppi.getTarjonta().getHaku().getOid();
        Sijoittelu sijoittelu = getOrCreateSijoittelu(hakuOid);
        SijoitteluAjo sijoitteluAjo = createSijoitteluAjo(sijoittelu);


        List<HakukohdeTyyppi> sisaantulevatHakukohteet = sijoitteluTyyppi.getTarjonta().getHakukohde();
        List<Hakukohde> hakukohteet = createHakukohteet(sisaantulevatHakukohteet, sijoitteluAjo);

        SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(hakukohteet);

        sijoitteluAjo.setStartMils(System.currentTimeMillis());
        sijoitteluAlgorithm.start();
        sijoitteluAjo.setEndMils(System.currentTimeMillis());

        // and after
        dao.persistSijoittelu(sijoittelu);
        for(Hakukohde hakukohde : hakukohteet ) {
            dao.persistHakukohde(hakukohde);
        }
    }

    private   List<Hakukohde> createHakukohteet( List<HakukohdeTyyppi> sisaantulevatHakukohteet, SijoitteluAjo sijoitteluAjo) {
        List<Hakukohde>  hakukohdes = new ArrayList<Hakukohde>();
        for(HakukohdeTyyppi hkt : sisaantulevatHakukohteet) {
            Hakukohde hakukohde = DomainConverter.convertToHakukohde(hkt);
            hakukohde.setSijoitteluajoId(sijoitteluAjo.getSijoitteluajoId());
            hakukohdes.add(hakukohde);

            HakukohdeItem hki = new HakukohdeItem();
            hki.setOid(hakukohde.getOid());
            sijoitteluAjo.getHakukohteet().add(hki);

        }
        return hakukohdes;
    }

    private SijoitteluAjo createSijoitteluAjo(Sijoittelu sijoittelu) {
        SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
        Long now = System.currentTimeMillis();
        sijoitteluAjo.setSijoitteluajoId(now);
        sijoittelu.getSijoitteluajot().add(sijoitteluAjo);
        return  sijoitteluAjo;
    }


    @Override
    public Valintatulos haeHakemuksenTila(String hakuoid,String hakukohdeOid, String valintatapajonoOid, String hakemusOid) {
        if (StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return dao.loadValintatuloksenTila(hakukohdeOid, valintatapajonoOid, hakemusOid);
    }

    @Override
    public void vaihdaHakemuksenTila(String hakuoid, String hakukohdeOid, String valintatapajonoOid, String hakemusOid, ValintatuloksenTila tila) {
        if (StringUtils.isBlank(hakuoid) ||StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(valintatapajonoOid) || StringUtils.isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }

        Sijoittelu sijoittelu = dao.loadSijoittelu(hakuoid);
        SijoitteluAjo ajo = sijoittelu.getLatestSijoitteluajo();
        Long ajoId = ajo.getSijoitteluajoId();

        Hakukohde hakukohde =  dao.getHakukohdeForSijoitteluajo(ajoId,hakukohdeOid );
        Valintatapajono valintatapajono = null;
        for(Valintatapajono v : hakukohde.getValintatapajonot()) {
            if(valintatapajonoOid.equals(v.getOid())) {
                valintatapajono = v;
                break;
            }
        }
        Hakemus hakemus = null;
        for(Hakemus h : valintatapajono.getHakemukset())  {
            if(hakemusOid.equals(h)) {
                hakemus = h;
            }
        }
        if(hakemus.getTila() != HakemuksenTila.HYVAKSYTTY) {
            throw new RuntimeException("sijoittelun hakemus ei ole hyvaksytty tilassa, fiksaa poikkeuskasittely myohemmin");
        }

        //TODO CHEKKAA ETTA TILAMUUTOS MAHDOLLINEN, HAE HAKIJAN OIDI
        Valintatulos v = dao.loadValintatuloksenTila(hakukohdeOid, valintatapajonoOid, hakemusOid);
        if (v == null) {
            v = new Valintatulos();
            v.setHakemusOid(hakemusOid);
            v.setValintatapajonoOid(valintatapajonoOid);
            v.setHakukohdeOid(hakukohdeOid);
            //TODO, LISAA HAKIJA OID
        }

        LOG.info("Asetetaan valintatuloksen tila - hakukohdeoid {}, valintatapajonooid {}, hakemusoid {}",
                new Object[]{hakukohdeOid, valintatapajonoOid, hakemusOid});
        LOG.info("Valintatuloksen uusi tila {}", tila.name());

        v.setTila(tila);
        dao.createOrUpdateValintatulos(v);
    }


    private Sijoittelu getOrCreateSijoittelu(String hakuoid) {
        Sijoittelu sijoittelu = dao.loadSijoittelu(hakuoid);
        if (sijoittelu == null) {
            sijoittelu = new Sijoittelu();
            sijoittelu.setCreated(new Date());
            sijoittelu.setSijoitteluId(System.currentTimeMillis());
            sijoittelu.setHakuOid(hakuoid);
        }
        return sijoittelu;
    }


}
