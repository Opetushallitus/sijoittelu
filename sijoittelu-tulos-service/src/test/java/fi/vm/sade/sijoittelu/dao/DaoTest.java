package fi.vm.sade.sijoittelu.dao;

import com.google.code.morphia.Datastore;
import fi.vm.sade.sijoittelu.dao.exception.SijoitteluEntityNotFoundException;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.util.DropMongoDbTestExecutionListener;
import fi.vm.sade.sijoittelu.util.TestDataGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, DropMongoDbTestExecutionListener.class})
public class DaoTest {

    @Autowired
    private DAO dao;

    private TestDataGenerator testDataGenerator;

    @Autowired
    private Datastore morphiaDS;

    @Before
    public void before() throws IOException {
        testDataGenerator = new TestDataGenerator(morphiaDS);
        testDataGenerator.generateTestData();
    }

    @Test
    public void testGetAllHakus() {
        List<Sijoittelu> hakus = dao.getHakus(null);
        assertEquals(1, hakus.size());
    }

    @Test
    public void testGetAllHakukohdes() {
        List<Hakukohde> hakukohdes = dao.getHakukohdes(TestDataGenerator.SIJOITTELU_AJO_ID_1, null);
        assertEquals(3, hakukohdes.size());
    }

    @Test
    public void testGetSijoitteluajo() {
        assertNotNull(dao.getSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_1));
    }

    @Test(expected = SijoitteluEntityNotFoundException.class)
    public void testGetNonExistingSijoitteluajo() {
        dao.getSijoitteluajo(-1);
    }

    @Test
    public void testGetAllSijoitteluajos() {
        List<SijoitteluAjo> sijoitteluajos = dao.getSijoitteluajos(null);
        assertEquals(2, sijoitteluajos.size());
    }

    @Test
    public void testGetSijoitteluByHakuOid() {
        Sijoittelu sijoittelu = dao.getSijoitteluByHakuOid(TestDataGenerator.HAKU_OID);
        assertNotNull(sijoittelu);

        assertNull(dao.getSijoitteluByHakuOid("notexists"));
    }
}
