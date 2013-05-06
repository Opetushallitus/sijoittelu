package fi.vm.sade.sijoittelu.resource;

import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dao.exception.SijoitteluEntityNotFoundException;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.tulos.resource.ObjectMapperProvider;
import fi.vm.sade.sijoittelu.tulos.resource.SijoitteluajoResource;
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

    @Test
    public void testGetHakukohdeBySijoitteluajo() throws IOException {
        final Long sijoitteluajoId = 1L;
        final String hakukohdeOid = "hakukohdeoid";
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid(hakukohdeOid);
        hakukohde.setTila(HakukohdeTila.SIJOITELTU);

        Valintatapajono jono = new Valintatapajono();
        jono.setAloituspaikat(10);
        jono.setOid("jonooid");
        jono.setPrioriteetti(5);
        jono.setTasasijasaanto(Tasasijasaanto.ARVONTA);
        jono.setTila(ValintatapajonoTila.SIJOITELTU);
        hakukohde.getValintatapajonot().add(jono);

        Hakemus hakemus = new Hakemus();
        hakemus.setHakijaOid("hakijaoid");
        hakemus.setJonosija(10);
        hakemus.setPrioriteetti(12);
        hakemus.setTasasijaJonosija(8);
        hakemus.setTila(HakemuksenTila.ILMOITETTU);
        jono.getHakemukset().add(hakemus);

        Hakijaryhma ryhma = new Hakijaryhma();
        ryhma.setNimi("hakijaryhma");
        ryhma.setOid("hakijaryhmaoid");
        ryhma.setPaikat(15);
        ryhma.setPrioriteetti(9);

        ryhma.getHakijaOid().add("hakijaoid");

        hakukohde.getHakijaryhmat().add(ryhma);

        Mockito.when(daoMock.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid)).thenReturn(hakukohde);


        Hakukohde haettu = sijoitteluajoResource.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid);
        String json = mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(haettu);
        Hakukohde fromJson = mapper.readValue(json, Hakukohde.class);

        assertEquals(hakukohde.getOid(), fromJson.getOid());
        assertEquals(hakukohde.getTila(), fromJson.getTila());
        assertEquals(hakukohde.getValintatapajonot().size(), fromJson.getValintatapajonot().size());

        for (int i = 0; i < hakukohde.getValintatapajonot().size(); ++i) {
            Valintatapajono j1 = hakukohde.getValintatapajonot().get(i);
            Valintatapajono j2 = fromJson.getValintatapajonot().get(i);
            assertEquals(j1.getAloituspaikat(), j2.getAloituspaikat());
            assertEquals(j1.getPrioriteetti(), j2.getPrioriteetti());
            assertEquals(j1.getOid(), j2.getOid());
            assertEquals(j1.getTasasijasaanto(), j2.getTasasijasaanto());
            assertEquals(j1.getTila(), j2.getTila());
            assertEquals(j1.getHakemukset().size(), j2.getHakemukset().size());

            for (int ii = 0; ii < j1.getHakemukset().size(); ++ii) {
                Hakemus h1 = j1.getHakemukset().get(ii);
                Hakemus h2 = j2.getHakemukset().get(ii);

                assertEquals(h1.getHakijaOid(), h2.getHakijaOid());
                assertEquals(h1.getJonosija(), h2.getJonosija());
                assertEquals(h1.getPrioriteetti(), h2.getPrioriteetti());
                assertEquals(h1.getTasasijaJonosija(), h2.getTasasijaJonosija());
                assertEquals(h1.getTila(), h2.getTila());
            }
        }

        assertEquals(hakukohde.getHakijaryhmat().size(), fromJson.getHakijaryhmat().size());
        for (int i = 0; i < hakukohde.getHakijaryhmat().size(); ++i) {
            Hakijaryhma hr1 = hakukohde.getHakijaryhmat().get(i);
            Hakijaryhma hr2 = fromJson.getHakijaryhmat().get(i);

            assertEquals(hr1.getNimi(), hr2.getNimi());
            assertEquals(hr1.getOid(), hr2.getOid());
            assertEquals(hr1.getPaikat(), hr2.getPaikat());
            assertEquals(hr1.getPrioriteetti(), hr2.getPrioriteetti());
            assertEquals(hr1.getHakijaOid().size(), hr2.getHakijaOid().size());

            for (int ii = 0; ii < hr1.getHakijaOid().size(); ++ii) {
                assertEquals(hr1.getHakijaOid().get(ii), hr2.getHakijaOid().get(ii));
            }
        }
    }
}
