package fi.vm.sade.sijoittelu;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import com.mongodb.MongoClientURI;

import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.laskenta.external.resource.VirkailijaValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.resource.SijoitteluResource;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluajoResourcesLoader;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintarekisteriService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This test class can be used to run sijoittelu with a data read from a server.
 * For more controlled tests, you can run unit tests such as <code>SijoitteleHakijaryhmaTest</code> or
 * <code>HakijaryhmaTest</code> , but if you're having a hard time reproducing a problem with hand crafted
 * test data, this class might help.
 *
 * You can compare the overriden fake implementation to <code>SijoitteluBusinessService</code> to see the
 * difference. Note that also some deps are mocked in the Spring configuration
 * file used here.
 *
 * Note: You need to
 *   ** add valintalaskentadb URL to run configuration VM parameters, e.g.
 *      <code>-ea -Xmx7G -DQA_VIRKAILIJAMONGO_URL=mongodb://valintalaskentauser:PASSWORD@localhost:37017</code>
 *      NB: be sure to tick off using <code>argLine</code> from Maven test runner IDEA settings, otherwise -Xmx512m will come
 *      to the command line from build-parent project that is the parent of valintaperusteet Maven project.
 *   ** add an ssh pipe to QA (or other similar environment) to access the /valintalaskentakoostepalvelu
 *      endpoint of valintaperusteet on the ALB, e.g. <code>ssh -L 1234:alb.testiopintopolku.fi:80 bastion.testiopintopolku.fi</code>
 *
 * @see SijoitteluBusinessService#sijoittele(HakuDTO, Set, Set, Long, Map)
 * @see HakijaryhmaTest
 * @see fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.SijoitteleHakijaryhmaTest
 */
@ContextConfiguration(locations = "classpath:test-sijoittelu-batch-real-valintalaskenta-mongo.xml")
@ExtendWith(SpringExtension.class)
@Disabled
public class SijoitteluIntegrationTestToBeRunManually {
    private static final Duration SIJOITTELU_STATE_POLL_INTERVAL = Duration.ofSeconds(30);

    static {
        if (System.getProperty("PIPED_HOST_VIRKAILIJA") == null) {
            String defaultValue = "http://must.be.accessed.with.something.dot.opintopolku.fi:12345";
            System.out.println("No PIPED_HOST_VIRKAILIJA system property set, defaulting to " + defaultValue);
            System.setProperty("PIPED_HOST_VIRKAILIJA", defaultValue);
        }
    }
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluIntegrationTestToBeRunManually.class);
    @Autowired
    private SijoitteluResource sijoitteluResource;

    @Autowired
    private LightWeightSijoitteluBusinessServiceForTesting lightWeightSijoitteluBusinessServiceForTesting;

    @Autowired
    @Qualifier("pipedHostVirkailija")
    private String pipedHostVirkailija;

    @Autowired
    @Qualifier("mongoUri2")
    private MongoClientURI mongoClientURI;

	@Test
	public void testSijoittelu() throws IOException, InterruptedException {
        ensureVirkailijaHostCanBeReached();
        LOG.info("Käytetään valintalaskentadb:tä klusterista " + mongoClientURI.getHosts());
        startSijoitteluAndPollUntilFinished();
        SijoitteluajoWrapper sijoitteluajoWrapper = lightWeightSijoitteluBusinessServiceForTesting.ajettuSijoittelu;
        MatcherAssert.assertThat(sijoitteluajoWrapper.getHakukohteet(), not(hasSize(0)));
        MatcherAssert.assertThat(sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot(), not(hasSize(0)));
        MatcherAssert.assertThat(sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset(), not(hasSize(0)));

        HakemusWrapper hakemusWrapper = findHakemusWrapper("1.2.246.562.20.33533744802", "1524747206010-8585833911442146692", "1.2.246.562.11.00012790612");
        System.out.println("hakemusWrapper = " + hakemusWrapper);
	}

    private void startSijoitteluAndPollUntilFinished() throws InterruptedException {
        Long sijoitteluajoId = sijoitteluResource.sijoittele("1.2.246.562.29.70000333388");
        while ("KESKEN".equals(sijoitteluResource.sijoittelunTila(sijoitteluajoId))) {
            System.out.println("Polled at " + new Date());
            Thread.sleep(SIJOITTELU_STATE_POLL_INTERVAL.toMillis());
        }
    }

    private HakemusWrapper findHakemusWrapper(String hakukohdeOid, String jonoOid, String hakemusOid) {
        HakukohdeWrapper hakukohdeWrapper = findHakukohdeWrapper(hakukohdeOid).orElseGet(() -> {
            throw new IllegalStateException("Could not find hakukohde " + hakukohdeOid);
        });
        ValintatapajonoWrapper jonoWrapper = hakukohdeWrapper.findValintatapajonoWrapper(jonoOid).orElseGet(() -> {
            throw new IllegalStateException("Could not find valintatapajono " + jonoOid + " of hakukohde " + hakukohdeWrapper.getHakukohde().getOid());
        });
        return jonoWrapper.findHakemus(hakemusOid).orElseGet(() -> {
            throw new IllegalStateException("Could not find hakemus " + hakemusOid +
                " of jono " + jonoWrapper.getValintatapajono().getOid() +
                " of hakukohde " + jonoWrapper.getHakukohdeWrapper().getHakukohde().getOid());
        });
    }

    private Optional<HakukohdeWrapper> findHakukohdeWrapper(String hakukohdeOid) {
        return lightWeightSijoitteluBusinessServiceForTesting.ajettuSijoittelu.getHakukohteet().stream().filter(hk -> hakukohdeOid.equals(hk.getHakukohde().getOid())).findFirst();
    }

    private void ensureVirkailijaHostCanBeReached() throws IOException {
        HttpGet request = new HttpGet(URI.create(pipedHostVirkailija + "/valintaperusteet-service/buildversion.txt"));
        LOG.info("Checking that piped ALB connection works so that " + request.getURI() + " can be reached, for accessing e.g. valintaperusteet");
        int timeoutMillis = 1500;
        CloseableHttpResponse response = HttpClientBuilder.create().setDefaultRequestConfig(
            RequestConfig.custom().setRedirectsEnabled(false).setConnectionRequestTimeout(timeoutMillis).setConnectTimeout(timeoutMillis).build()).build().execute(request);
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode(), IOUtils.toString(response.getEntity().getContent(), "UTF-8"));
    }

    public static class LightWeightSijoitteluBusinessServiceForTesting extends SijoitteluBusinessService {
        public SijoitteluajoWrapper ajettuSijoittelu;

        @Autowired
        public LightWeightSijoitteluBusinessServiceForTesting(SijoitteluTulosConverter sijoitteluTulosConverter, TarjontaIntegrationService tarjontaIntegrationService, VirkailijaValintaTulosServiceResource valintaTulosServiceResource, ValintarekisteriService valintarekisteriService) {
            super(sijoitteluTulosConverter,
                valintaTulosServiceResource,
                valintarekisteriService,
                new SijoitteluConfiguration(),
                new SijoitteluajoResourcesLoader(tarjontaIntegrationService, valintarekisteriService));
        }

        /**
         * This is a very lightweight version of sijoittelu, without using any previous results, without storing
         * results etc.
         * The results might or might not make sense depending on your use case.
         */
        @Override
        public void sijoittele(HakuDTO haku, Set<String> eiSijoitteluunMenevatJonot, Set<String> laskennanTuloksistaJaValintaperusteistaLoytyvatJonot, Long sijoittelunTunniste, Map<String, Map<String, ValintatapajonoDTO>> hakukohdeMapToValintatapajonoByOid) {
            String hakuOid = haku.getHakuOid();
            String nameOfThisFakeSijoitteluRun = "Haun " + hakuOid + " " + LightWeightSijoitteluBusinessServiceForTesting.class.getSimpleName() + " -sijoittelu";
            StopWatch stopWatch = new StopWatch(nameOfThisFakeSijoitteluRun);

            stopWatch.start("Päätellään hakukohde- ja valintatapajonotiedot");
            List<Hakukohde> uudetHakukohteet = haku.getHakukohteet().stream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
            stopWatch.stop();

            SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
            sijoitteluAjo.setSijoitteluajoId(sijoittelunTunniste);
            sijoitteluAjo.setHakuOid(hakuOid);
            LOG.info(nameOfThisFakeSijoitteluRun + " sijoittelun koko: 0 olemassaolevaa, {} uutta, 0 valintatulosta", uudetHakukohteet.size());
            stopWatch.start("Luodaan sijoitteluajoWrapper ja asetetaan parametrit");
            final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), sijoitteluAjo, uudetHakukohteet, Collections.emptyMap());
            sijoitteluajoWrapper.paivitaVastaanottojenVaikutusHakemustenTiloihin(Collections.emptyList(), Collections.emptyMap());
            sijoitteluajoResourcesLoader.asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper, sijoitteluajoResourcesLoader.findParametersFromTarjontaAndPerformInitialValidation(hakuOid, new StopWatch(""), ""));
            stopWatch.stop();

            LOG.info("Suoritetaan " + nameOfThisFakeSijoitteluRun);
            stopWatch.start("Suoritetaan sijoittelu");
            SijoitteluAlgorithmUtil.sijoittele(sijoitteluajoWrapper);
            stopWatch.stop();

            LOG.info(stopWatch.prettyPrint());
            ajettuSijoittelu = sijoitteluajoWrapper;
        }

        @Override
        public List<HakukohdeDTO> valisijoittele(HakuDTO sijoitteluTyyppi) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long erillissijoittele(HakuDTO haku) {
            throw new UnsupportedOperationException();
        }
    }
}
