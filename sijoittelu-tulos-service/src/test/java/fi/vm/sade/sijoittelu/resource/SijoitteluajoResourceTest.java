package fi.vm.sade.sijoittelu.resource;

import fi.vm.sade.sijoittelu.dao.DAO;
import fi.vm.sade.sijoittelu.dao.exception.SijoitteluEntityNotFoundException;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
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
 * Date: 30.4.2013
 * Time: 9.48
 */
public class SijoitteluajoResourceTest {
    private ObjectMapper mapper = new ObjectMapperProvider().getContext(SijoitteluAjo.class);

    private SijoitteluajoResource sijoitteluajoResource;
    private DAO daoMock;

    @Before
    public void setUp() {
        daoMock = Mockito.mock(DAO.class);

        sijoitteluajoResource = new SijoitteluajoResource();
        ReflectionTestUtils.setField(sijoitteluajoResource, "dao", daoMock);
    }

    @Test
    public void testGetSijoitteluajoById() throws IOException {
        final Long sijoitteluajoId = 1L;
        SijoitteluAjo ajo = new SijoitteluAjo();
        ajo.setEndMils(new Date().getTime());
        ajo.setStartMils(new Date().getTime());
        ajo.setSijoitteluajoId(sijoitteluajoId);
        for (int i = 0; i < 2; ++i) {
            HakukohdeItem hk = new HakukohdeItem();
            hk.setOid("oid " + i);
            ajo.getHakukohteet().add(hk);
        }

        Mockito.when(daoMock.getSijoitteluajo(sijoitteluajoId)).thenReturn(ajo);


        SijoitteluAjo haettu = sijoitteluajoResource.getSijoitteluajo(sijoitteluajoId);
        String json = mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(haettu);
        SijoitteluAjo fromJson = mapper.readValue(json, SijoitteluAjo.class);

        assertEquals(ajo.getEndMils(), fromJson.getEndMils());
        assertEquals(ajo.getStartMils(), fromJson.getStartMils());
        assertEquals(ajo.getSijoitteluajoId(), fromJson.getSijoitteluajoId());
        assertEquals(ajo.getHakukohteet().size(), fromJson.getHakukohteet().size());

        assertEquals(ajo.getHakukohteet().get(0).getOid(), fromJson.getHakukohteet().get(0).getOid());
        assertEquals(ajo.getHakukohteet().get(1).getOid(), fromJson.getHakukohteet().get(1).getOid());
    }

    @Test(expected = WebApplicationException.class)
    public void testGetSijoitteluajoByIdNotFound() {
        try {

            Mockito.when(daoMock.getSijoitteluajo(Mockito.anyLong()))
                    .thenThrow(new SijoitteluEntityNotFoundException());

            sijoitteluajoResource.getSijoitteluajo(1L);
        } catch (WebApplicationException e) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), e.getResponse().getStatus());
            throw e;
        }
    }
}
