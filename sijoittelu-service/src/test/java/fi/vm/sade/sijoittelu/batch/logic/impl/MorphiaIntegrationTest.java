package fi.vm.sade.sijoittelu.batch.logic.impl;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;

import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValiSijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.tarjonta.service.resources.v1.dto.HakuV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Kari Kammonen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-sijoittelu-batch-mongo.xml" })
@ActiveProfiles()
public class MorphiaIntegrationTest {

	@Autowired
	private ValintatulosDao valintatulosDao;

    @Autowired
    private SijoitteluDao sijoitteluDao;

    @Autowired
    private HakukohdeDao hakukohdeDao;

    @Autowired
    private ValiSijoitteluDao valisijoitteluDao;

	@Autowired
	private SijoitteluBusinessService sijoitteluService;


    @Autowired
    private TarjontaIntegrationService tarjontaIntegrationService;

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test");

    @Test
    public void testValintatuloksenPersistointi() {
        final String hakemusOid = "HAKEMUSOID";
        final String hakijaOid = "HAKIJAOID";
        final String hakukohdeOid = "HAKUKOHDEOID";
        final int hakutoive = 5;
        final boolean hyvaksyPeruuntunut = true;
        final boolean hyvaksyttyVarasijalta = true;
        final IlmoittautumisTila ilmoittautumistila = IlmoittautumisTila.LASNA;
        final boolean julkaistavissa = true;
        final ValintatuloksenTila tila = ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT;
        final String jonoOid = "JONOOID";
        final LogEntry log = new LogEntry();
        final String muokkaaja = "MUOKKAAJA";
        final String selite = "SELITE";
        final String muutos = "MUUTOS";
        final Date now = DateTime.now().toDate();
        log.setLuotu(now);
        log.setMuokkaaja(muokkaaja);
        log.setMuutos(muutos);
        log.setSelite(selite);
        Valintatulos v0 = new Valintatulos();
        v0.setHakemusOid(hakemusOid, "");
        v0.setHakijaOid(hakijaOid, "");
        v0.setHakukohdeOid(hakukohdeOid, "");
        v0.setHakutoive(hakutoive, "");
        v0.setHyvaksyPeruuntunut(hyvaksyPeruuntunut, "");
        v0.setHyvaksyttyVarasijalta(hyvaksyttyVarasijalta, "");
        v0.setIlmoittautumisTila(ilmoittautumistila, "");
        v0.setJulkaistavissa(julkaistavissa, "");
        v0.setTila(tila, "");
        v0.setValintatapajonoOid(jonoOid, "");
        valintatulosDao.createOrUpdateValintatulos(v0);

        Valintatulos vx = valintatulosDao.loadValintatulos(hakukohdeOid,jonoOid,hakemusOid);
        Assert.assertEquals(vx.getId(), v0.getId());
        Assert.assertEquals(vx.getHakemusOid(), v0.getHakemusOid());
        Assert.assertEquals(vx.getHakijaOid(), v0.getHakijaOid());
        Assert.assertEquals(vx.getHakuOid(), v0.getHakuOid());
        Assert.assertEquals(vx.getValintatapajonoOid(), v0.getValintatapajonoOid());
        Assert.assertEquals(vx.getHakutoive(), v0.getHakutoive());
        Assert.assertEquals(vx.getHyvaksyPeruuntunut(), v0.getHyvaksyPeruuntunut());
        Assert.assertEquals(vx.getHyvaksyttyVarasijalta(), v0.getHyvaksyttyVarasijalta());
        Assert.assertEquals(vx.getIlmoittautumisTila(), v0.getIlmoittautumisTila());
        Assert.assertEquals(vx.getJulkaistavissa(), v0.getJulkaistavissa());
        Assert.assertEquals(vx.getTila(), v0.getTila());
        Assert.assertEquals(vx.getLogEntries(), v0.getLogEntries());

        vx.setHakemusOid(v0.getHakemusOid() + "X", "");
        vx.setTila(ValintatuloksenTila.ILMOITETTU, "");
        valintatulosDao.createOrUpdateValintatulos(vx);

        Valintatulos vx2 = valintatulosDao.loadValintatulos(hakukohdeOid,jonoOid,hakemusOid);
        Assert.assertEquals(vx2.getId(), vx.getId());
        Assert.assertEquals(vx2.getTila(), vx.getTila());
        Assert.assertNotSame(vx2.getHakemusOid(), vx.getHakemusOid());
        Assert.assertEquals(vx2.getLogEntries(), vx.getLogEntries());
    }

	@Test
	public void testSijoitteluService() {

        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);

        ReflectionTestUtils.setField(sijoitteluService,
                "tarjontaIntegrationService",
                tarjontaIntegrationService);


        String hakuJson = "{\n" +
                "    \"hakukausiUri\": \"kausi_s#1\", \n" +
                "    \"hakukausiVuosi\": 2014, \n" +
                "    \"hakutapaUri\": \"hakutapa_01#1\", \n" +
                "    \"hakutyyppiUri\": \"hakutyyppi_01#1\", \n" +
                "    \"kohdejoukkoUri\": \"haunkohdejoukko_11#1\", \n" +
                "    \"modifiedBy\": \"1.2.246.562.24.15473337696\", \n" +
                "    \"oid\": \"1.2.246.562.29.92175749016\"\n" +
                "  }";
        HakuV1RDTO haku = new GsonBuilder().create().fromJson(hakuJson,  new TypeToken<HakuV1RDTO>() {
        }.getType());

        ResultV1RDTO<HakuV1RDTO> dto = new ResultV1RDTO<>();
        dto.setResult(haku);

        when(tarjontaIntegrationService.getHaunKohdejoukko(anyString())).thenReturn(Optional.of("haunkohdejoukko_11"));

        String json = "{ \"target\": \"1.2.246.562.29.173465377510\", \"__modified__\": 1416309364472, \"__modifiedBy__\": \"1.2.246.562.24.47840234552\", \"PH_TJT\": {\"date\": null}, \"PH_HKLPT\": {\"date\": null}, \"PH_HKMT\": {\"date\": null}, \"PH_KKM\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_HVVPTP\": {\"date\": null}, \"PH_KTT\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_OLVVPKE\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_VLS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_SS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_JKLIP\": {\"date\": null}, \"PH_HKP\": {\"date\": 1416866395389}, \"PH_VTSSV\": {\"date\": 1416866395389}, \"PH_VSSAV\": {\"date\": 1416866458888}, \"PH_VTJH\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_EVR\": {\"date\": null}, \"PH_OPVP\": {\"date\": null}, \"PH_HPVOA\": {\"date\": null}, \"PH_IP\": {\"date\": null} }";

        when(tarjontaIntegrationService.getHaunParametrit(anyString())).thenReturn(new GsonBuilder().create().fromJson(json, ParametriDTO.class));

		HakuDTO st = new HakuDTO();

		st.setHakuOid("testihakuoidi");

		sijoitteluService.sijoittele(st, new HashSet<>());
		Sijoittelu sijoittelu = sijoitteluDao.getSijoitteluByHakuOid("testihakuoidi").get();
		Assert.assertNotNull(sijoittelu);
		Assert.assertEquals(sijoittelu.getHakuOid(), "testihakuoidi");

	}

    @Configuration
    @ComponentScan("fi.vm.sade.sijoittelu.laskenta.service.business.impl")
    static class someConfig {

        @Bean
        PropertyPlaceholderConfigurer propConfig() {
            PropertyPlaceholderConfigurer ppc =  new PropertyPlaceholderConfigurer();
            ppc.setLocation(new ClassPathResource("common.properties"));
            return ppc;
        }
    }

}
