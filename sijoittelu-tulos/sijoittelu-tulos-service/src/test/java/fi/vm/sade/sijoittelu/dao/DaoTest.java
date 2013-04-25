package fi.vm.sade.sijoittelu.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.sijoittelu.dao.exception.SijoitteluEntityNotFoundException;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.util.AbstractSijoitteluMongoDbTest;
import fi.vm.sade.sijoittelu.util.TestDataGenerator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class DaoTest extends AbstractSijoitteluMongoDbTest {

    @Autowired
    private DAO dao;

    @Autowired
    private TestDataGenerator testDataGenerator;

    @Before
    public void before() {
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
}
