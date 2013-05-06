
package fi.vm.sade.sijoittelu.batch.logic.impl;

import fi.vm.sade.service.sijoittelu.SijoitteluService;
import fi.vm.sade.service.sijoittelu.schema.TarjontaTyyppi;
import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.tarjonta.service.types.HakuTyyppi;
import fi.vm.sade.tulos.service.types.tulos.SijoitteluajoTyyppi;
import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.sijoittelu.batch.dao.Dao;
import fi.vm.sade.sijoittelu.batch.logic.impl.itutil.FlapdoodleMongoDbTest;

/**
 * 
 * @author Kari Kammonen
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-sijoittelu-batch-mongo.xml" })
@ActiveProfiles()
public class MorphiaIntegrationTest extends FlapdoodleMongoDbTest {

    @Autowired
    private Dao dao;

    @Autowired
    private SijoitteluService sijoitteluService;

    @Test
    public void testSijoitteluService() {

        SijoitteleTyyppi st = new SijoitteleTyyppi();
        st.setTarjonta(new TarjontaTyyppi());
        st.getTarjonta().setHaku(new HakuTyyppi());
        st.getTarjonta().getHaku().setOid("testihakuoidi");

        sijoitteluService.sijoittele(st);
        Sijoittelu sijoittelu = dao.loadSijoittelu("testihakuoidi");
        Assert.assertNotNull(sijoittelu);
        Assert.assertEquals(sijoittelu.getHakuOid(), "testihakuoidi");
    }

}
