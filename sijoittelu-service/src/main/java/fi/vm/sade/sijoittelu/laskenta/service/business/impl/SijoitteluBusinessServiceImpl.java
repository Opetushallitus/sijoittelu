package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactory;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.dao.Dao;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public void sijoittele(SijoitteleTyyppi sijoitteluTyyppi) {

        String hakuOid = sijoitteluTyyppi.getTarjonta().getHaku().getOid();

        Sijoittelu sijoittelu = getOrCreateSijoittelu(sijoitteluTyyppi.getTarjonta().getHaku().getOid());

        List<HakukohdeTyyppi> hakukohde = sijoitteluTyyppi.getTarjonta().getHakukohde();
        SijoitteluAjo sijoitteluAjo = DomainConverter.convertToSijoitteluAjo(hakukohde);

        Long now = System.currentTimeMillis();
        sijoitteluAjo.setSijoitteluajoId(now);
        sijoitteluAjo.setStartMils(now);

        // persist domain before algorithm starts
        dao.persistSijoitteluAjo(sijoitteluAjo);
        sijoittelu.getSijoitteluajot().add(sijoitteluAjo);
        dao.persistSijoittelu(sijoittelu);

        //  if (sijoittelu.isSijoittele()
        // ) {
        SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(sijoitteluAjo);
        sijoitteluAlgorithm.start();
        //     System.out.println(   PrintHelper.tulostaSijoittelu(sijoitteluAlgorithm));
        //  }


        sijoitteluAjo.setEndMils(System.currentTimeMillis());

        // and after
        dao.persistSijoitteluAjo(sijoitteluAjo);
    }

    @Override
    public Valintatulos haeHakemuksenTila(String hakukohdeOid, String valintatapajonoOid, String hakemusOid) {
        if (StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return dao.loadValintatuloksenTila(hakukohdeOid, valintatapajonoOid, hakemusOid);
    }

    @Override
    public void vaihdaHakemuksenTila(String hakukohdeOid, String valintatapajonoOid, String hakemusOid, ValintatuloksenTila tila) {
        if (StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
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
