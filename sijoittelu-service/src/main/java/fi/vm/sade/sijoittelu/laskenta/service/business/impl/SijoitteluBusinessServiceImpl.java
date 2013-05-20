package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import java.util.Date;
import java.util.List;

import fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.laskenta.dao.Dao;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactory;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Service
public class SijoitteluBusinessServiceImpl implements SijoitteluBusinessService {

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
         System.out.println(   PrintHelper.tulostaSijoittelu(sijoitteluAlgorithm));
      //  }


        sijoitteluAjo.setEndMils(System.currentTimeMillis());

        // and after
        dao.persistSijoitteluAjo(sijoitteluAjo);
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
