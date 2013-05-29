package fi.vm.sade.sijoittelu.resource;

import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dao.exception.SijoitteluEntityNotFoundException;
import fi.vm.sade.sijoittelu.tulos.resource.ObjectMapperProvider;
import fi.vm.sade.sijoittelu.tulos.resource.SijoitteluResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
            sijoitteluResource.getSijoitteluByHakuOid(hakuOidNotExists);
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

        // Haku haku = new Haku();
        toReturn.setHakuOid(hakuOidExists);
        // toReturn.setHaku(haku);

        Mockito.when(daoMock.getSijoitteluByHakuOid(hakuOidExists)).thenReturn(toReturn);

        Sijoittelu sijoittelu = sijoitteluResource.getSijoitteluByHakuOid(hakuOidExists);
        String json = mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(sijoittelu);
        Sijoittelu fromJson = mapper.readValue(json, Sijoittelu.class);

        assertEquals(sijoittelu.getCreated(), fromJson.getCreated());
        assertEquals(sijoittelu.getSijoitteluId(), fromJson.getSijoitteluId());
        assertEquals(sijoittelu.getHakuOid(), fromJson.getHakuOid());
    }
   /*
    @Test
    public void testGetSijoitteluajoByHakuOidNotExists() {
        final String hakuOidNotExists = "hakuoidNotExists";
        Mockito.when(daoMock.getSijoitteluajoByHakuOid(hakuOidNotExists)).thenThrow(new SijoitteluEntityNotFoundException());
        assertEquals(0, sijoitteluResource.getSijoitteluajoByHakuOid(hakuOidNotExists, false).size());
    }
     */
    /*
    @Test
    public void testGetSijoitteluajoByHakuOid() throws IOException {
        final String hakuOidExists = "hakuoidExists";

        final Calendar now = Calendar.getInstance();
        final Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);

        SijoitteluAjo s = new SijoitteluAjo();
        s.setEndMils(now.getTimeInMillis());
        s.setStartMils(yesterday.getTimeInMillis());
        s.setSijoitteluajoId(1L);

        for (int i = 0; i < 2; ++i) {
            HakukohdeItem hk = new HakukohdeItem();
            hk.setOid("hakuoid" + i);
            s.getHakukohteet().add(hk);
        }

        List<SijoitteluAjo> toReturn = new ArrayList<SijoitteluAjo>();
        toReturn.add(s);

        Mockito.when(daoMock.getSijoitteluajoByHakuOid(hakuOidExists)).thenReturn(toReturn);

        List<SijoitteluAjo> sijoitteluajo = sijoitteluResource.getSijoitteluajoByHakuOid(hakuOidExists, false);
        String json = mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(sijoitteluajo);
        List<SijoitteluAjo> fromJson = mapper.readValue(json, new TypeReference<List<SijoitteluAjo>>() {
        });

        SijoitteluAjo sa = fromJson.get(0);
        assertEquals(s.getSijoitteluajoId(), sa.getSijoitteluajoId());
        assertEquals(s.getEndMils(), sa.getEndMils());
        assertEquals(s.getStartMils(), sa.getStartMils());
        assertEquals(s.getHakukohteet().size(), sa.getHakukohteet().size());

        for (int i = 0; i < s.getHakukohteet().size(); ++i) {
            HakukohdeItem hk = s.getHakukohteet().get(0);
            HakukohdeItem hka = sa.getHakukohteet().get(0);
            assertEquals(hk.getOid(), hka.getOid());
        }

    }
    */
}
