package fi.vm.sade.sijoittelu.laskenta.service.business.processors;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.LogEntry;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.external.resource.ValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanottoEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostSijoitteluProcessorValintaTulosServiceValinnantilojenTallennus implements PostSijoitteluProcessor {
    private static final String SIJOITTELU_AS_ILMOITTAJA = "järjestelmä";
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorValintaTulosServiceValinnantilojenTallennus.class);
    private final ValintaTulosServiceResource valintaTulosServiceResource;

    public PostSijoitteluProcessorValintaTulosServiceValinnantilojenTallennus(ValintaTulosServiceResource valintaTulosServiceResource) {
        this.valintaTulosServiceResource = valintaTulosServiceResource;
    }

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        LOG.info("(hakuOid={}) Tallennetaan valinnantilat Valinta-tulos-service -palveluun", sijoitteluajoWrapper.getSijoitteluajo().getHakuOid());

        Stream<VastaanottoEventDto> muuttuneetValintatuloksetEventteina =
                sijoitteluajoWrapper.getMuuttuneetValintatulokset().stream().map(v -> new VastaanottoEventDto(
                        v.getHakijaOid(), v.getHakemusOid(), v.getHakukohdeOid(), v.getHakuOid(), v.getTila(), SIJOITTELU_AS_ILMOITTAJA, extractSeliteFromValintatulos(v)));
        valintaTulosServiceResource.valintatuloksetValinnantilalla(muuttuneetValintatuloksetEventteina.collect(Collectors.toList()));

    }
    private static String extractSeliteFromValintatulos(Valintatulos valintatulos) {
        return valintatulos.getLogEntries().stream().findFirst().map(LogEntry::getSelite).orElse("");
    }
}
