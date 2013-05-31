package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import fi.vm.sade.service.valintatiedot.schema.HakuTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
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

import java.util.*;

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


    //ei versioi sijoittelua, tekeee uuden sijoittelun olemassaoleville kohteille
    public void sijoittele(String hakuOid) {

        Sijoittelu sijoittelu = getOrCreateSijoittelu(hakuOid);
        SijoitteluAjo viimeisinSijoitteluajo = sijoittelu.getLatestSijoitteluajo();
        List<Hakukohde> hakukohteet = dao.getHakukohdeForSijoitteluajo(viimeisinSijoitteluajo.getSijoitteluajoId());
        List<Valintatulos> valintatulokset = dao.loadValintatulokset(hakuOid);
        SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(hakukohteet, valintatulokset);

        viimeisinSijoitteluajo.setStartMils(System.currentTimeMillis());
        sijoitteluAlgorithm.start();
        viimeisinSijoitteluajo.setEndMils(System.currentTimeMillis());

        // and after
        dao.persistSijoittelu(sijoittelu);
        for(Hakukohde hakukohde : hakukohteet ) {
            dao.persistHakukohde(hakukohde);
        }
    }

    //verioi sijoittelun ja tuo uudet kohteet
    @Override
    public void sijoittele(HakuTyyppi sijoitteluTyyppi) {

        System.out.println(PrintHelper.tulostaSijoittelu(sijoitteluTyyppi));

        String hakuOid = sijoitteluTyyppi.getHakuOid();
        Sijoittelu sijoittelu = getOrCreateSijoittelu(hakuOid);
        SijoitteluAjo viimeisinSijoitteluajo = sijoittelu.getLatestSijoitteluajo();

        List<Hakukohde> uudetHakukohteet = convertHakukohteet(sijoitteluTyyppi.getHakukohteet());
        List<Hakukohde> olemassaolevatHakukohteet = Collections.<Hakukohde>emptyList();
        if(viimeisinSijoitteluajo != null) {
            olemassaolevatHakukohteet = dao.getHakukohdeForSijoitteluajo(viimeisinSijoitteluajo.getSijoitteluajoId());
        }
        SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(sijoittelu);
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet);


        List<Valintatulos> valintatulokset = dao.loadValintatulokset(hakuOid);
        SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(kaikkiHakukohteet,valintatulokset);

        uusiSijoitteluajo.setStartMils(System.currentTimeMillis());
        System.out.println(PrintHelper.tulostaSijoittelu(sijoitteluAlgorithm));
        sijoitteluAlgorithm.start();
        uusiSijoitteluajo.setEndMils(System.currentTimeMillis());

        // and after
        dao.persistSijoittelu(sijoittelu);
        for(Hakukohde hakukohde : kaikkiHakukohteet ) {
            dao.persistHakukohde(hakukohde);
        }
    }

    //nykyisellaan vain korvaa hakukohteet, mietittava toiminta tarkemmin
    private List<Hakukohde> merge(SijoitteluAjo uusiSijoitteluajo, List<Hakukohde> olemassaolevatHakukohteet, List<Hakukohde> uudetHakukohteet) {
        Map<String,Hakukohde> kaikkiHakukohteet = new HashMap<String, Hakukohde>();
        for(Hakukohde hakukohde : olemassaolevatHakukohteet) {
            hakukohde.setId(null);       //poista id vanhoilta hakukohteilta, niin etta ne voidaan peristoida uusina dokumentteina
            kaikkiHakukohteet.put(hakukohde.getOid(), hakukohde);
        }
        //ylikirjoita uusilla kohteilla kylmasti
        for(Hakukohde hakukohde : uudetHakukohteet) {
            kaikkiHakukohteet.put(hakukohde.getOid(), hakukohde);
        }

        for(Hakukohde hakukohde : kaikkiHakukohteet.values()) {
            HakukohdeItem hki = new HakukohdeItem();
            hki.setOid(hakukohde.getOid());
            uusiSijoitteluajo.getHakukohteet().add(hki);
            hakukohde.setSijoitteluajoId(uusiSijoitteluajo.getSijoitteluajoId());
        }

        return new ArrayList<Hakukohde>(kaikkiHakukohteet.values());
    }

    private   List<Hakukohde> convertHakukohteet( List<HakukohdeTyyppi> sisaantulevatHakukohteet) {
        List<Hakukohde>  hakukohdes = new ArrayList<Hakukohde>();
        for(HakukohdeTyyppi hkt : sisaantulevatHakukohteet) {
            Hakukohde hakukohde = DomainConverter.convertToHakukohde(hkt);
            hakukohdes.add(hakukohde);
        }
        return hakukohdes;
    }

    private SijoitteluAjo createSijoitteluAjo(Sijoittelu sijoittelu) {
        SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
        Long now = System.currentTimeMillis();
        sijoitteluAjo.setSijoitteluajoId(now);
        sijoitteluAjo.setHakuOid(sijoittelu.getHakuOid()); //silta varalta etta tehdaan omaksi entityksi
        sijoittelu.getSijoitteluajot().add(sijoitteluAjo);
        return  sijoitteluAjo;
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


    @Override
    public Valintatulos haeHakemuksenTila(String hakuoid,String hakukohdeOid, String valintatapajonoOid, String hakemusOid) {
        if (StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return dao.loadValintatulos(hakukohdeOid, valintatapajonoOid, hakemusOid);
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
            if(hakemusOid.equals(h.getHakemusOid())) {
                hakemus = h;
            }
        }
        if(hakemus.getTila() != HakemuksenTila.HYVAKSYTTY) {
            throw new RuntimeException("sijoittelun hakemus ei ole hyvaksytty tilassa, fiksaa poikkeuskasittely myohemmin");
        }

        Valintatulos v = dao.loadValintatulos(hakukohdeOid, valintatapajonoOid, hakemusOid);
        if (v == null) {
            v = new Valintatulos();
            v.setHakemusOid(hakemus.getHakemusOid());
            v.setValintatapajonoOid(valintatapajono.getOid());
            v.setHakukohdeOid(hakukohde.getOid());
            v.setHakijaOid(hakemus.getHakijaOid());
            v.setHakutoive(hakemus.getPrioriteetti());
            v.setHakuOid(hakuoid);
        }

        LOG.info("Asetetaan valintatuloksen tila - hakukohdeoid {}, valintatapajonooid {}, hakemusoid {}",
                new Object[]{hakukohdeOid, valintatapajonoOid, hakemusOid});
        LOG.info("Valintatuloksen uusi tila {}", tila.name());

        v.setTila(tila);
        dao.createOrUpdateValintatulos(v);
    }




}
