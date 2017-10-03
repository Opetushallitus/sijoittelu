package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class SijoitteluTestSpec {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluTestSpec.class);

    public Function<HakuDTO,SijoitteluajoWrapper> algoritmi() {
        return (haku) -> {
            List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
            final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohteet, Collections.emptyList(), Collections.emptyMap());
            SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);
            LOG.debug("\r\n{}", PrintHelper.tulostaSijoittelu(s));
            return sijoitteluAjo;
        };
    }

    public SijoitteluajoWrapper ajaSijoittelu(String filename) {
        HakuDTO t = TestHelper.readHakuDTOFromJson(filename);
        return algoritmi().apply(t);
    }
}
