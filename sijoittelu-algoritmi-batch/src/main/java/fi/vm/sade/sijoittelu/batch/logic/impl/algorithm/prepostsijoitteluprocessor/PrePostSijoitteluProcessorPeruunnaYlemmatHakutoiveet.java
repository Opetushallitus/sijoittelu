package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.prepostsijoitteluprocessor;


import com.google.common.collect.Sets;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.Timer;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet implements PreSijoitteluProcessor, PostSijoitteluProcessor {

    private static final Set<HakemuksenTila> hakemuksenHyvaksytytTilat = Sets.newHashSet(
        HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY, HakemuksenTila.PERUNUT, HakemuksenTila.PERUUTETTU
    );

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
                vtjs.stream()
                    .flatMap(vtj -> vtj.getHakemukset().stream())
                    .collect(Collectors.groupingBy(Hakemus::getHakijaOid, Collectors.mapping(Function.identity(), Collectors.toList())))
                    .entrySet().parallelStream().forEach(es -> {
                    List<Hakemus> hakijanHakemukset = es.getValue();
                    hakijanHakemukset.stream()
                        .filter(h -> hakemuksenHyvaksytytTilat.contains(h.getTila()))
                        .forEach(hh -> hakijanHakemukset.stream()
                            .filter(hah -> hah.getPrioriteetti() > hh.getPrioriteetti())
                            .forEach(yph -> {
                                if (yph.getTila() == HakemuksenTila.VARALLA) {
                                    yph.setTilanKuvaukset(TilanKuvaukset.peruuntunutHyvaksyttyAlemmallaHakutoiveella());
                                    yph.setTila(HakemuksenTila.PERUUNTUNUT);
                                    peruunnutetutHakemukset.incrementAndGet();
                                }
                            }));

                });
            }
            timer.stop("AMKOPE-haulle " + hakuOid + ", peruunnutettu " + peruunnutetutHakemukset + " kpl");
        }
    }
}
