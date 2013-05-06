package fi.vm.sade.sijoittelu.it;

import com.google.code.morphia.Datastore;
import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.resource.ObjectMapperProvider;
import fi.vm.sade.sijoittelu.tulos.resource.SijoitteluResource;
import fi.vm.sade.sijoittelu.util.DropMongoDbTestExecutionListener;
import fi.vm.sade.sijoittelu.util.TestDataGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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
import java.util.List;

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
        Sijoittelu sijoittelu = sijoitteluResource.getSijoitteluByHakuOid(TestDataGenerator.HAKU_OID);

        String json = mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(sijoittelu);
        Sijoittelu fromJson = mapper.readValue(json, Sijoittelu.class);

        assertEquals(sijoittelu.getCreated(), fromJson.getCreated());
        assertEquals(sijoittelu.getSijoitteluId(), fromJson.getSijoitteluId());
        assertEquals(sijoittelu.getHakuOid(), fromJson.getHakuOid());
    }

    @Test
    public void testGetSijoitteluajoByHakuOid() throws IOException {
        List<SijoitteluAjo> sijoitteluajos =
                sijoitteluResource.getSijoitteluajoByHakuOid(TestDataGenerator.HAKU_OID, false);

        String json = mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(sijoitteluajos);
        List<SijoitteluAjo> fromJson = mapper.readValue(json, new TypeReference<List<SijoitteluAjo>>() {
        });
    }

    @Test
    public void testGetLatestSijoitteluajoByHakuOid() throws IOException {
        List<SijoitteluAjo> sijoitteluajos =
                sijoitteluResource.getSijoitteluajoByHakuOid(TestDataGenerator.HAKU_OID, true);

        assertEquals(1, sijoitteluajos.size());
        assertEquals(TestDataGenerator.SIJOITTELU_AJO_ID_2, sijoitteluajos.get(0).getSijoitteluajoId());
        String json = mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(sijoitteluajos);
        List<SijoitteluAjo> fromJson = mapper.readValue(json, new TypeReference<List<SijoitteluAjo>>() {
        });
    }
}
