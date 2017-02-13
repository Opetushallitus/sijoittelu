package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.prepostsijoitteluprocessor;


import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.Timer;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet implements PreSijoitteluProcessor, PostSijoitteluProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        if (sijoitteluajoWrapper.isAmkopeHaku() && sijoitteluajoWrapper.varasijaSaannotVoimassa() ) {
            final String hakuOid = sijoitteluajoWrapper.getSijoitteluajo().getHakuOid();
            final AtomicInteger peruunnutetutHakemukset = new AtomicInteger(0);

            final Timer timer = Timer.start("Pre/Post-processor peruunna ylemmät hakutoiveet", "AMKOPE-haulle " + hakuOid, PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet.class);

            List<Valintatapajono> vtjs = sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(hkv -> hkv.getValintatapajonot().stream())
                .map(ValintatapajonoWrapper::getValintatapajono).collect(Collectors.toList());

            if (vtjs.stream().allMatch(Valintatapajono::getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa)) {
                List<Hakemus> peruunnutettavatHakemukset = vtjs.stream()
                    .flatMap(vtj -> vtj.getHakemukset().stream())
                    .collect(Collectors.groupingBy(Hakemus::getHakijaOid, Collectors.mapping(Function.identity(), Collectors.toList())))
                    .entrySet().parallelStream().flatMap(es -> {
                        List<Hakemus> hakijanHakemukset = es.getValue();
                        return hakijanHakemukset.stream()
                            .filter(h -> TilaTaulukot.kuuluuPeruunnutettaviinTiloihin(h.getTila()))
                            .flatMap(hyvaksyttyHakemus -> hakijanHakemukset.stream()
                                .filter(hakijanHakemus -> hakijanHakemus.getPrioriteetti() < hyvaksyttyHakemus.getPrioriteetti())
                                .filter(ylemmanPrioriteetinHakemus -> ylemmanPrioriteetinHakemus.getTila() == HakemuksenTila.VARALLA));
                    }).collect(Collectors.toList());

                peruunnutettavatHakemukset.forEach(h -> {
                    h.setTilanKuvaukset(TilanKuvaukset.peruuntunutHyvaksyttyAlemmallaHakutoiveella());
                    h.setTila(HakemuksenTila.PERUUNTUNUT);
                    peruunnutetutHakemukset.incrementAndGet();
                });
                LOG.info("Peruunnutettu hakemukset: {}", peruunnutettavatHakemukset.stream().map(Hakemus::getHakemusOid).collect(Collectors.joining(", ")));
            }
            timer.stop("AMKOPE-haulle " + hakuOid + ", peruunnutettu " + peruunnutetutHakemukset + " kpl");
        }
    }
}
