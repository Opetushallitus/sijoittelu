package fi.vm.sade.sijoittelu.laskenta.service.business.processors;

import com.google.common.collect.MultimapBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.external.resource.ValintaTulosServiceResource;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        Map<MultiKey, List<Valintatulos>> valintatulokset = valintaTulosServiceResource.valintatuloksetValinnantilalla(sijoitteluajoWrapper.getSijoitteluajo().getHakuOid()).stream()
                .collect(Collectors.groupingBy(o -> new MultiKey(o.getValintatapajonoOid(), o.getHakemusOid(), o.getHakukohdeOid())));
        sijoitteluajoWrapper.getHakukohteet().stream().flatMap(h -> h.hakukohteenHakijat()).flatMap(h -> h.getValintatulos().stream()).map(v -> {
            ValintatuloksenTila valintatuloksenTila = valintatulokset.get(new MultiKey(v.getValintatapajonoOid(), v.getHakemusOid(), v.getHakukohdeOid())).stream().map(vv -> vv.getTila()).findAny().orElse(ValintatuloksenTila.KESKEN);
            v.setTila(valintatuloksenTila, "");
            return v;
        });
    }
}
