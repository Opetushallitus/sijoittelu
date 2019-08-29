package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.prepostsijoitteluprocessor;


import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus;
import fi.vm.sade.sijoittelu.domain.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.Timer;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet implements PreSijoitteluProcessor, PostSijoitteluProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        if (sijoitteluajoWrapper.isAmkopeHaku() && sijoitteluajoWrapper.varasijaSaannotVoimassa() ) {
            final String hakuOid = sijoitteluajoWrapper.getSijoitteluajo().getHakuOid();
            final AtomicInteger peruunnutetutHakemukset = new AtomicInteger(0);

            final Timer timer = Timer.start("Pre/Post-processor peruunna ylemmät hakutoiveet", "AMKOPE-haulle " + hakuOid, PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet.class);

            if (sijoitteluajoWrapper.getHakukohteet().stream()
                    .flatMap(hkv -> hkv.getValintatapajonot().stream())
                    .allMatch(v -> v.getValintatapajono().getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa())) {
                List<HakemusWrapper> peruunnutettavatHakemukset = sijoitteluajoWrapper.getHakukohteet().stream()
                        .flatMap(hk -> hk.getValintatapajonot().stream())
                        .flatMap(vtj -> vtj.getHakemukset().stream())
                        .collect(Collectors.groupingBy(h -> h.getHakemus().getHakemusOid()))
                        .values().parallelStream()
                        .flatMap(this::hyvaksyttyaYlemmatVarallaOlevat)
                        .collect(Collectors.toList());

                peruunnutettavatHakemukset.forEach(h -> {
                    TilojenMuokkaus.asetaTilaksiPeruuntunutAlempiToive(h);
                    h.setTilaVoidaanVaihtaa(false);
                    peruunnutetutHakemukset.incrementAndGet();
                });
                LOG.info("Peruunnutettu hakemukset: {}", peruunnutettavatHakemukset.stream().map(h -> h.getHakemus().getHakemusOid()).collect(Collectors.joining(", ")));
            }
            timer.stop("AMKOPE-haulle " + hakuOid + ", peruunnutettu " + peruunnutetutHakemukset + " kpl");
        }
    }

    private Stream<HakemusWrapper> hyvaksyttyaYlemmatVarallaOlevat(List<HakemusWrapper> hakemusValintatapajonoissa) {
        return hakemusValintatapajonoissa.stream()
                .filter(h -> TilaTaulukot.kuuluuPeruunnutettaviinTiloihin(h.getHakemus().getTila()))
                .flatMap(hyvaksyttyHakemus -> hakemusValintatapajonoissa.stream()
                        .filter(h -> h.getHakemus().getPrioriteetti() < hyvaksyttyHakemus.getHakemus().getPrioriteetti())
                        .filter(ylemmanPrioriteetinHakemus -> ylemmanPrioriteetinHakemus.getHakemus().getTila() == HakemuksenTila.VARALLA));
    }
}
