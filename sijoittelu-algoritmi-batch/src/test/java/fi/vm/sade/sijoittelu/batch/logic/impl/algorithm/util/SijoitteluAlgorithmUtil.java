package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.*;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SijoitteluAlgorithmUtil {
    public static SijoittelunTila sijoittele(List<Hakukohde> hakukohteet,
                                             List<Valintatulos> valintatulokset,
                                             Map<String, VastaanottoDTO> aiemmanVastaanotonHakukohdePerHakija) {
        return sijoittele(PreSijoitteluProcessor.defaultPreProcessors(),
            PostSijoitteluProcessor.defaultPostProcessors(),
            new SijoitteluAjo(),
            hakukohteet,
            valintatulokset,
            aiemmanVastaanotonHakukohdePerHakija);
    }

    public static SijoittelunTila sijoittele(Collection<PreSijoitteluProcessor> preProcessors,
                                              Collection<PostSijoitteluProcessor> postProcessors,
                                              SijoitteluAjo sijoitteluAjo,
                                              List<Hakukohde> hakukohteet,
                                              List<Valintatulos> valintatulokset,
                                              Map<String, VastaanottoDTO> aiemmanVastaanotonHakukohdePerHakija) {
        SijoitteluajoWrapper wrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(sijoitteluAjo, hakukohteet,
                valintatulokset, aiemmanVastaanotonHakukohdePerHakija);
        wrapper.setKKHaku(true);
        return SijoitteluAlgorithm.sijoittele(preProcessors, postProcessors, wrapper);
    }

    public static SijoittelunTila sijoittele(SijoitteluajoWrapper ajo) {
        return SijoitteluAlgorithm.sijoittele(PreSijoitteluProcessor.defaultPreProcessors(),
            PostSijoitteluProcessor.defaultPostProcessors(),
            ajo);
    }
}
