package fi.vm.sade.sijoittelu.batch.logic.impl;

import java.util.Date;
import java.util.List;

import fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.sijoittelu.batch.dao.Dao;
import fi.vm.sade.sijoittelu.batch.logic.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactory;
import fi.vm.sade.sijoittelu.domain.Haku;
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
        Sijoittelu sijoittelu = getOrCreateSijoittelu(sijoitteluTyyppi);

        List<HakukohdeTyyppi> hakukohde = sijoitteluTyyppi.getTarjonta().getHakukohde();
        SijoitteluAjo sijoitteluAjo = DomainConverter.convertToSijoitteluAjo(hakukohde);

        Long now = System.currentTimeMillis();
        sijoitteluAjo.setSijoitteluajoId(now);
        sijoitteluAjo.setStartMils(now);

        // persist domain before algorithm starts
        dao.persistSijoitteluAjo(sijoitteluAjo);
        sijoittelu.getSijoitteluajot().add(sijoitteluAjo);
        dao.persistSijoittelu(sijoittelu);

        if (sijoittelu.isSijoittele()) {
            SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(sijoitteluAjo);
            sijoitteluAlgorithm.start();
        }
        sijoitteluAjo.setEndMils(System.currentTimeMillis());

        // and after
        dao.persistSijoitteluAjo(sijoitteluAjo);
    }

    private Sijoittelu getOrCreateSijoittelu(SijoitteleTyyppi sijoitteluTyyppi) {
        Sijoittelu sijoittelu = dao.loadSijoittelu(Long.valueOf(sijoitteluTyyppi.getSijoitteluId()));
        if (sijoittelu == null) {
            sijoittelu = new Sijoittelu();
            sijoittelu.setCreated(new Date());

            // FIXME: Mmmiksi tänne menee String?
            sijoittelu.setSijoitteluId(Long.valueOf(sijoitteluTyyppi.getSijoitteluId()));
            sijoittelu.setHaku(new Haku());
            sijoittelu.getHaku().setOid(sijoitteluTyyppi.getTarjonta().getHaku().getOid());
            sijoittelu.setSijoittele(sijoitteluTyyppi.getTarjonta().getHaku().isSijoittelu());
        }
        return sijoittelu;
    }

}
