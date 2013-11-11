
package fi.vm.sade.sijoittelu.batch.logic.impl;

import fi.vm.sade.service.sijoittelu.SijoitteluService;
import fi.vm.sade.service.valintatiedot.schema.HakuTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.itutil.FlapdoodleMongoDbTest;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.laskenta.dao.Dao;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

        HakuTyyppi st = new HakuTyyppi();

        st.setHakuOid("testihakuoidi");

        sijoitteluService.sijoittele(st);
        Sijoittelu sijoittelu = dao.loadSijoittelu("testihakuoidi");
        Assert.assertNotNull(sijoittelu);
        Assert.assertEquals(sijoittelu.getHakuOid(), "testihakuoidi");
    }

}
