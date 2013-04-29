package fi.vm.sade.sijoittelu.resource;

import fi.vm.sade.sijoittelu.dao.DAO;
import fi.vm.sade.sijoittelu.domain.Haku;
import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * User: wuoti
 * Date: 26.4.2013
 * Time: 15.24
 */
public class SijoitteluResourceTest {

    private ObjectMapper mapper = new ObjectMapperProvider().getContext(SijoitteluResource.class);

    private SijoitteluResource sijoitteluResource;
    private DAO daoMock;

    @Before
    public void setUp() {
        daoMock = Mockito.mock(DAO.class);

        sijoitteluResource = new SijoitteluResource();
        ReflectionTestUtils.setField(sijoitteluResource, "dao", daoMock);
    }

    @Test(expected = WebApplicationException.class)
    public void testGetSijoitteluByHakuOidNotExists() {
        try {
            final String hakuOidNotExists = "hakuoidNotExists";
            Mockito.when(daoMock.getSijoitteluByHakuOid(hakuOidNotExists)).thenReturn(null);
            sijoitteluResource.getSijoitteluByHakuoid(hakuOidNotExists);
        } catch (WebApplicationException e) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), e.getResponse().getStatus());
            throw e;
        }
    }

    @Test
    public void testGetSijoitteluByHakuOid() throws IOException {
        final String hakuOidExists = "hakuoidExists";

        Sijoittelu toReturn = new Sijoittelu();
        toReturn.setCreated(new Date());
        toReturn.setSijoittele(true);
        toReturn.setSijoitteluId(1L);

        Haku haku = new Haku();
        haku.setOid(hakuOidExists);
        toReturn.setHaku(haku);

        Mockito.when(daoMock.getSijoitteluByHakuOid(hakuOidExists)).thenReturn(toReturn);

        Sijoittelu sijoittelu = sijoitteluResource.getSijoitteluByHakuoid(hakuOidExists);
        String json = mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(sijoittelu);
        Sijoittelu fromJson = mapper.readValue(json, Sijoittelu.class);

        assertEquals(sijoittelu.getCreated(), fromJson.getCreated());
        assertEquals(sijoittelu.getSijoitteluId(), fromJson.getSijoitteluId());
        assertEquals(sijoittelu.getHaku().getOid(), fromJson.getHaku().getOid());
    }
}
