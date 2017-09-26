package fi.vm.sade.sijoittelu;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

import fi.vm.sade.service.valintaperusteet.resource.ValintalaskentakoostepalveluResource;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
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
import fi.vm.sade.sijoittelu.laskenta.service.business.ActorService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintarekisteriService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
 *      <code>-ea -Xmx7G -DQA_VIRKAILIJAMONGO_URL=mongodb://oph:PASSWORD@qa-mongodb1.oph.ware.fi,qa-mongodb2.oph.ware.fi,qa-mongodb3.oph.ware.fi:27017</code>
 *   ** add an ssh pipe to QA (or other similar environment) to access the /valintalaskentakoostepalvelu
 *      endpoint of valintaperusteet, e.g. <code>ssh -L 1234:testi.virkailija.opintopolku.fi:443 ruokala</code>
 *   ** change the local loopback IP address to point to the server you want to access, e.g. add this to
 *      <code>/etc/hosts</code> : <code>127.0.0.1       testi.virkailija.opintopolku.fi</code>
 *
 * @see SijoitteluBusinessService#sijoittele(HakuDTO, Set, Set)
 * @see HakijaryhmaTest
 * @see fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.SijoitteleHakijaryhmaTest
 */
@ContextConfiguration(locations = "classpath:test-sijoittelu-batch-real-valintalaskenta-mongo.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@UsingDataSet
@Ignore
public class SijoitteluIntegrationTestToBeRunManually {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluIntegrationTestToBeRunManually.class);
    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ValintalaskentakoostepalveluResource valintalaskentakoostepalveluResource;

    @Autowired
    private SijoitteluResource sijoitteluResource;

    @Autowired
    private LightWeightSijoitteluBusinessServiceForTesting lightWeightSijoitteluBusinessServiceForTesting;

	@Test
	public void testSijoittelu() throws IOException {
        sijoitteluResource.sijoittele("1.2.246.562.29.87593180141");
        SijoitteluajoWrapper sijoitteluajoWrapper = lightWeightSijoitteluBusinessServiceForTesting.ajettuSijoittelu;
        assertThat(sijoitteluajoWrapper.getHakukohteet(), not(hasSize(0)));
        assertThat(sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot(), not(hasSize(0)));
        assertThat(sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset(), not(hasSize(0)));

        HakemusWrapper hakemusWrapper = findHakemusWrapper("1.2.246.562.20.12440626997", "14751510516182446649292011171466", "1.2.246.562.11.00007467028");
        System.out.println("hakemusWrapper = " + hakemusWrapper);
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

    public static class LightWeightSijoitteluBusinessServiceForTesting extends SijoitteluBusinessService {
        public SijoitteluajoWrapper ajettuSijoittelu;

        @Autowired
        public LightWeightSijoitteluBusinessServiceForTesting(SijoitteluTulosConverter sijoitteluTulosConverter, ActorService actorService, TarjontaIntegrationService tarjontaIntegrationService, VirkailijaValintaTulosServiceResource valintaTulosServiceResource, ValintarekisteriService valintarekisteriService) {
            super(sijoitteluTulosConverter, actorService, tarjontaIntegrationService, valintaTulosServiceResource, valintarekisteriService);
        }

        /**
         * This is a very lightweight version of sijoittelu, without using any previous results, without storing
         * results etc.
         * The results might or might not make sense depending on your use case.
         */
        @Override
        public void sijoittele(HakuDTO haku, Set<String> eiSijoitteluunMenevatJonot, Set<String> valintaperusteidenValintatapajonot) {
            String hakuOid = haku.getHakuOid();
            String nameOfThisFakeSijoitteluRun = "Haun " + hakuOid + " " + LightWeightSijoitteluBusinessServiceForTesting.class.getSimpleName() + " -sijoittelu";
            StopWatch stopWatch = new StopWatch(nameOfThisFakeSijoitteluRun);

            stopWatch.start("Päätellään hakukohde- ja valintatapajonotiedot");
            List<Hakukohde> uudetHakukohteet = haku.getHakukohteet().stream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
            stopWatch.stop();

            SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
            sijoitteluAjo.setSijoitteluajoId(System.currentTimeMillis());
            sijoitteluAjo.setHakuOid(hakuOid);
            LOG.info(nameOfThisFakeSijoitteluRun + " sijoittelun koko: 0 olemassaolevaa, {} uutta, 0 valintatulosta", uudetHakukohteet.size());
            stopWatch.start("Luodaan sijoitteluajoWrapper ja asetetaan parametrit");
            final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(sijoitteluAjo, uudetHakukohteet, Collections.emptyList(), Collections.emptyMap());
            asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper);
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
