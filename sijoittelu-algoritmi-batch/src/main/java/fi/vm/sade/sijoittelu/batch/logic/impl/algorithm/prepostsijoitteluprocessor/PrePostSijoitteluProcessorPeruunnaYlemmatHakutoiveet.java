package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.prepostsijoitteluprocessor;


import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.Timer;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
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

            List<ValintatapajonoWrapper> vtjs = sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(hkv -> hkv.getValintatapajonot().stream())
                .collect(Collectors.toList());

            if (vtjs.stream().allMatch(v -> v.getValintatapajono().getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa())) {
                List<HakemusWrapper> peruunnutettavatHakemukset = vtjs.stream()
                        .flatMap(vtj -> vtj.getHakemukset().stream())
                        .collect(Collectors.groupingBy(h -> h.getHakemus().getHakemusOid()))
                        .values().parallelStream().flatMap(hakemusValintatapajonoissa -> hakemusValintatapajonoissa.stream()
                                .filter(h -> TilaTaulukot.kuuluuPeruunnutettaviinTiloihin(h.getHakemus().getTila()))
                                .flatMap(hyvaksyttyHakemus -> hakemusValintatapajonoissa.stream()
                                        .filter(h -> h.getHakemus().getPrioriteetti() < hyvaksyttyHakemus.getHakemus().getPrioriteetti())
                                        .filter(ylemmanPrioriteetinHakemus -> ylemmanPrioriteetinHakemus.getHakemus().getTila() == HakemuksenTila.VARALLA))).collect(Collectors.toList());

                peruunnutettavatHakemukset.forEach(h -> {
                    h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHyvaksyttyAlemmallaHakutoiveella());
                    h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                    h.setTilaVoidaanVaihtaa(false);
                    peruunnutetutHakemukset.incrementAndGet();
                });
                LOG.info("Peruunnutettu hakemukset: {}", peruunnutettavatHakemukset.stream().map(h -> h.getHakemus().getHakemusOid()).collect(Collectors.joining(", ")));
            }
            timer.stop("AMKOPE-haulle " + hakuOid + ", peruunnutettu " + peruunnutetutHakemukset + " kpl");
        }
    }
}
