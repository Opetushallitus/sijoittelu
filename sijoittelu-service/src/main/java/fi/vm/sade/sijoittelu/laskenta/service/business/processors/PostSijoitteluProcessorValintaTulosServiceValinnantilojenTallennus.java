package fi.vm.sade.sijoittelu.laskenta.service.business.processors;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.laskenta.external.resource.ValintaTulosServiceResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostSijoitteluProcessorValintaTulosServiceValinnantilojenTallennus implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorValintaTulosServiceValinnantilojenTallennus.class);
    private final ValintaTulosServiceResource valintaTulosServiceResource;

    public PostSijoitteluProcessorValintaTulosServiceValinnantilojenTallennus(ValintaTulosServiceResource valintaTulosServiceResource) {
        this.valintaTulosServiceResource = valintaTulosServiceResource;
    }

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        LOG.info("(hakuOid={}) Tallennetaan valinnantilat Valinta-tulos-service -palveluun", sijoitteluajoWrapper.getSijoitteluajo().getHakuOid());

        sijoitteluajoWrapper.getMuuttuneetValintatulokset();
    }
}
