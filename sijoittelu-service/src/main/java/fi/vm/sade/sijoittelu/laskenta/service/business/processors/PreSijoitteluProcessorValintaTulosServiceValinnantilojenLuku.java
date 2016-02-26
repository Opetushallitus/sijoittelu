package fi.vm.sade.sijoittelu.laskenta.service.business.processors;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.laskenta.external.resource.ValintaTulosServiceResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class PreSijoitteluProcessorValintaTulosServiceValinnantilojenLuku implements PreSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PreSijoitteluProcessorValintaTulosServiceValinnantilojenLuku.class);
    private final ValintaTulosServiceResource valintaTulosServiceResource;

    public PreSijoitteluProcessorValintaTulosServiceValinnantilojenLuku(ValintaTulosServiceResource valintaTulosServiceResource) {
        this.valintaTulosServiceResource = valintaTulosServiceResource;
    }

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        LOG.info("(hakuOid={}) Haetaan valinnantilat Valinta-tulos-service -palvelusta", sijoitteluajoWrapper.getSijoitteluajo().getHakuOid());
        //valintaTulosServiceResource.vastaanotettavuus()
        Stream<String> hakemukset = sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(hk -> hk.getValintatapajonot().stream())
                .flatMap(j -> j.getHakemukset().stream())
                .flatMap(h -> h.getValintatulos().map(xx -> Stream.of(xx.getHakemusOid())).orElse(Stream.empty()));

    }
}
