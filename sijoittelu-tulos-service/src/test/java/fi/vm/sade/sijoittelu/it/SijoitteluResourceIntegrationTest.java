package fi.vm.sade.sijoittelu.it;

import com.google.code.morphia.Datastore;
import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.resource.ObjectMapperProvider;
import fi.vm.sade.sijoittelu.resource.SijoitteluResource;
import fi.vm.sade.sijoittelu.util.DropMongoDbTestExecutionListener;
import fi.vm.sade.sijoittelu.util.TestDataGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * User: wuoti
 * Date: 26.4.2013
 * Time: 16.13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, DropMongoDbTestExecutionListener.class})
public class SijoitteluResourceIntegrationTest {

    @Autowired
    private SijoitteluResource sijoitteluResource;

    private TestDataGenerator testDataGenerator;

    private ObjectMapper mapper = new ObjectMapperProvider().getContext(SijoitteluResource.class);

    @Autowired
    private Datastore morphiaDS;

    @Before
    public void before() throws IOException {
        testDataGenerator = new TestDataGenerator(morphiaDS);
        testDataGenerator.generateTestData();
    }

    @Test
    public void testGetSijoitteluByHakuOid() throws IOException {
        Sijoittelu sijoittelu = sijoitteluResource.getSijoitteluByHakuoid(TestDataGenerator.HAKU_OID);

        String json = mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(sijoittelu);
        Sijoittelu fromJson = mapper.readValue(json, Sijoittelu.class);

        assertEquals(sijoittelu.getCreated(), fromJson.getCreated());
        assertEquals(sijoittelu.getSijoitteluId(), fromJson.getSijoitteluId());
        assertEquals(sijoittelu.getHaku().getOid(), fromJson.getHaku().getOid());
    }

}
