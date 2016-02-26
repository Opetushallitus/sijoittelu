package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.*;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

import java.util.List;

public class SijoitteluAlgorithmUtil {

    public static SijoittelunTila sijoittele(SijoitteluAjo ajo, List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset) {
        return SijoitteluAlgorithm.sijoittele(PreSijoitteluProcessor.defaultPreProcessors(), PostSijoitteluProcessor.defaultPostProcessors(), new SijoitteluAjo(), hakukohteet, valintatulokset);
    }
    public static SijoittelunTila sijoittele(SijoitteluajoWrapper ajo) {
        return SijoitteluAlgorithm.sijoittele(PreSijoitteluProcessor.defaultPreProcessors(), PostSijoitteluProcessor.defaultPostProcessors(), ajo);
    }
}
