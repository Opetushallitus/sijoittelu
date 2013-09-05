/*package fi.vm.sade.sijoittelu.dao;

import fi.vm.sade.sijoittelu.util.DropMongoDbTestExecutionListener;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, DropMongoDbTestExecutionListener.class})
public class DaoTest {
    /*
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
    public void testGetSijoitteluajo() {
        assertNotNull(dao.getSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_1));
    }

    @Test
    public void  testGetSijoittelu() {
        Assert.assertEquals(1, dao.getSijoittelu().size());
    }

    @Test
    public void testGetHakukohdeBySijoitteluajo() {
        Assert.assertNotNull(    dao.getHakukohdeBySijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_1, TestDataGenerator.HAKUKOHDE_OID_1));
    }


    @Test
    public void testGetSijoitteluByHakuOid() {
        Sijoittelu sijoittelu = dao.getSijoitteluByHakuOid(TestDataGenerator.HAKU_OID);
        assertNotNull(sijoittelu);
        assertNull(dao.getSijoitteluByHakuOid("notexists"));
    }




}
                    */