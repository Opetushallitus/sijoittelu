package fi.vm.sade.sijoittelu;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactory;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakuV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.OhjausparametriResource;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValiSijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.tarjonta.service.resources.v1.dto.HakuV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: wuoti Date: 11.11.2013 Time: 15.13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-sijoittelu-batch-mongo.xml" })
public class SijoitteluBusinessTest {

    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluBusinessTest.class);

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
    private HakuV1Resource hakuV1Resource;

    @Autowired
    private OhjausparametriResource ohjausparametriResource;

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test");

    @Test
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    @Ignore
    public void testPeruutaAlemmat() throws IOException {

        hakuV1Resource = mock(HakuV1Resource.class);
        ohjausparametriResource = mock(OhjausparametriResource.class);

        ReflectionTestUtils.setField(sijoitteluService,
                "hakuV1Resource",
                hakuV1Resource);

        ReflectionTestUtils.setField(sijoitteluService,
                "ohjausparametriResource",
                ohjausparametriResource);

        String hakuJson = "{\n" +
                "    \"hakukausiUri\": \"kausi_s#1\", \n" +
                "    \"hakukausiVuosi\": 2014, \n" +
                "    \"hakutapaUri\": \"hakutapa_01#1\", \n" +
                "    \"hakutyyppiUri\": \"hakutyyppi_01#1\", \n" +
                "    \"kohdejoukkoUri\": \"haunkohdejoukko_11#1\", \n" +
                "    \"modifiedBy\": \"1.2.246.562.24.15473337696\", \n" +
                "    \"oid\": \"1.2.246.562.29.92175749016\"\n" +
                "  }";
        HakuV1RDTO hakuRDTO = new GsonBuilder().create().fromJson(hakuJson,  new TypeToken<HakuV1RDTO>() {
        }.getType());

        ResultV1RDTO<HakuV1RDTO> dto = new ResultV1RDTO<>();
        dto.setResult(hakuRDTO);

        when(hakuV1Resource.findByOid(anyString())).thenReturn(dto);

        String json = "{ \"target\": \"1.2.246.562.29.173465377510\", \"__modified__\": 1416309364472, \"__modifiedBy__\": \"1.2.246.562.24.47840234552\", \"PH_TJT\": {\"date\": null}, \"PH_HKLPT\": {\"date\": null}, \"PH_HKMT\": {\"date\": null}, \"PH_KKM\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_HVVPTP\": {\"date\": null}, \"PH_KTT\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_OLVVPKE\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_VLS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_SS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_JKLIP\": {\"date\": null}, \"PH_HKP\": {\"date\": 14168663953898}, \"PH_VTSSV\": {\"date\": 1416866395389}, \"PH_VSSAV\": {\"date\": 1416866458888}, \"PH_VTJH\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_EVR\": {\"date\": null}, \"PH_OPVP\": {\"date\": null}, \"PH_HPVOA\": {\"date\": null}, \"PH_IP\": {\"date\": null} }";

        when(ohjausparametriResource.haePaivamaara(anyString())).thenReturn(json);

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku);


        Optional<SijoitteluAjo> latestSijoitteluajo = sijoitteluDao.getLatestSijoitteluajo("haku1");
        System.out.println("SijoitteluajoID: " + latestSijoitteluajo.get().getSijoitteluajoId());

        hakukohdeDao.getHakukohdeForSijoitteluajo(latestSijoitteluajo.get().getSijoitteluajoId()).forEach(hakukohde -> {
            System.out.println("HAKUKOHDE: " + hakukohde.getOid());
            hakukohde.getValintatapajonot().forEach(jono -> {
                System.out.println("  JONO: " + jono.getOid());
                jono.getHakemukset().forEach(hakemus -> {
                    System.out.println(hakemus.getHakemusOid() + " " + hakemus.getTila().name() + " " + hakemus.getPrioriteetti());
                });
            });
        });

        haku.getHakukohteet().remove(0);

        sijoitteluService.sijoittele(haku);

        latestSijoitteluajo = sijoitteluDao.getLatestSijoitteluajo("haku1");
        System.out.println("SijoitteluajoID: " + latestSijoitteluajo.get().getSijoitteluajoId());

        hakukohdeDao.getHakukohdeForSijoitteluajo(latestSijoitteluajo.get().getSijoitteluajoId()).forEach(hakukohde -> {
            System.out.println("HAKUKOHDE: " + hakukohde.getOid());
            hakukohde.getValintatapajonot().forEach(jono -> {
                System.out.println("  JONO: " + jono.getOid());
                jono.getHakemukset().forEach(hakemus -> {
                    System.out.println("    " + hakemus.getHakemusOid() + " " + hakemus.getTila().name() + " " + hakemus.getPrioriteetti());
                });
            });
        });

        haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku);

        latestSijoitteluajo = sijoitteluDao.getLatestSijoitteluajo("haku1");
        System.out.println("SijoitteluajoID: " + latestSijoitteluajo.get().getSijoitteluajoId());

        hakukohdeDao.getHakukohdeForSijoitteluajo(latestSijoitteluajo.get().getSijoitteluajoId()).forEach(hakukohde -> {
            System.out.println("HAKUKOHDE: " + hakukohde.getOid());
            hakukohde.getValintatapajonot().forEach(jono -> {
                System.out.println("  JONO: " + jono.getOid());
                jono.getHakemukset().forEach(hakemus -> {
                    System.out.println("    " + hakemus.getHakemusOid() + " " + hakemus.getTila().name() + " " + hakemus.getPrioriteetti());
                });
            });
        });

    }

//    @Configuration
//    @ComponentScan("fi.vm.sade.sijoittelu.laskenta.service.business.impl")
//    static class someConfig {
//
//        @Bean
//        PropertyPlaceholderConfigurer propConfig() {
//            PropertyPlaceholderConfigurer ppc =  new PropertyPlaceholderConfigurer();
//            ppc.setLocation(new ClassPathResource("common.properties"));
//            return ppc;
//        }
//    }

}
