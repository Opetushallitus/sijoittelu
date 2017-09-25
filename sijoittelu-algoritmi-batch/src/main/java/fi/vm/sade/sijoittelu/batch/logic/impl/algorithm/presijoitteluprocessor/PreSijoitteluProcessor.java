package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import com.google.common.collect.ImmutableList;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.Processor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.prepostsijoitteluprocessor.PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

import java.util.Collection;

public interface PreSijoitteluProcessor extends Processor {

    void process(SijoitteluajoWrapper sijoitteluajoWrapper);

    static Collection<PreSijoitteluProcessor> defaultPreProcessors() {
        return ImmutableList.of(
                new PreSijoitteluProcessorAsetaSivssnov(),
                new PreSijoitteluProcessorKiilaavatHakemuksenVaralleRajatunVarasijataytonJonoissa(),
                new PreSijoitteluProcessorPidaPeruuntuneinaSivssnovHakemuksetRajatunVarasijataytonJonoissa(),
                new PreSijoitteluProcessorTasasijaArvonta(),
                new PreSijoitteluProcessorSort(),
                new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt(),
                new PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat(),
                new PreSijoitteluProcessorJarjesteleAloituspaikatTayttojonoihin(),
                new PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet(),
                new PreSijoitteluProcessorLahtotilanteenHash()
        );
    }
}
