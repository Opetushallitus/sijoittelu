/*package fi.vm.sade.sijoittelu.it;

import org.mongodb.morphia.Datastore;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.resource.ObjectMapperProvider;
import fi.vm.sade.sijoittelu.tulos.resource.SijoitteluajoResource;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, DropMongoDbTestExecutionListener.class})
public class SijoitteluajoResourceIntegrationTest {

    @Autowired
    private SijoitteluajoResource sijoitteluajoResource;

    private TestDataGenerator testDataGenerator;

    private ObjectMapper mapper = new ObjectMapperProvider().getContext(SijoitteluajoResource.class);

    @Autowired
    private Datastore morphiaDS;

    @Before
    public void before() throws IOException {
        testDataGenerator = new TestDataGenerator(morphiaDS);
        testDataGenerator.generateTestData();
    }

    @Test
    public void testGetSijoitteluajoById() throws IOException {
        SijoitteluAjo haettu = sijoitteluajoResource.getSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_1);
        String json = mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(haettu);
        mapper.readValue(json, SijoitteluAjo.class);
    }

    @Test
    public void testGetHakukohdeBySijoitteluajo() throws IOException {
        Hakukohde haettu = sijoitteluajoResource
                .getHakukohdeBySijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_1, TestDataGenerator.HAKUKOHDE_OID_1);
        String json = mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(haettu);
        mapper.readValue(json, Hakukohde.class);
    }
} */