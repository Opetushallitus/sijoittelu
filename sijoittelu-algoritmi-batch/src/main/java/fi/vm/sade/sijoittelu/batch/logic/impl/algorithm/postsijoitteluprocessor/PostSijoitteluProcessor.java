package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import com.google.common.collect.ImmutableList;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.Processor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.prepostsijoitteluprocessor.PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

import java.util.Collection;

public interface PostSijoitteluProcessor extends Processor {

    void process(SijoitteluajoWrapper sijoitteluajoWrapper);

    static Collection<PostSijoitteluProcessor> defaultPostProcessors() {
        return ImmutableList.of(
                new PostSijoitteluProcessorAsetaSivssnov(),
                new PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus(),
                new PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet(),
                new PostSijoitteluProcessorMuutostiedonAsetus()
        );
    }
}
