package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakukohdeWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.SijoitteleHakukohde;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.*;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public abstract class SijoitteluAlgorithm {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluAlgorithm.class);

    public static SijoittelunTila sijoittele(Collection<PreSijoitteluProcessor> preProcessors,Collection<PostSijoitteluProcessor> postProcessors, SijoitteluajoWrapper sijoitteluAjo) {
        LOG.info("(hakuOid={}) Starting sijoitteluajo {}",
                Optional.ofNullable(sijoitteluAjo.getSijoitteluajo()).orElse(new SijoitteluAjo()).getHakuOid(), sijoitteluAjo.getSijoitteluAjoId());
        preProcessors.stream().map(log(sijoitteluAjo)).forEach(p -> p.process(sijoitteluAjo));
        final SijoittelunTila tila = suoritaSijoittelu(sijoitteluAjo);
        postProcessors.stream().map(log(sijoitteluAjo)).forEach(p -> p.process(sijoitteluAjo));
        return tila;
    }
    private static <T extends Processor> Function<T,T> log(final SijoitteluajoWrapper sijoitteluAjo) {
        return (processor) -> {
            LOG.info("(hakuOid={}) Starting processor {} for sijoitteluAjo {}",
                    Optional.ofNullable(sijoitteluAjo.getSijoitteluajo()).orElse(new SijoitteluAjo()).getHakuOid(), processor.name(), sijoitteluAjo.getSijoitteluAjoId());
            return processor;
        };
    }

    private static SijoittelunTila suoritaSijoittelu(final SijoitteluajoWrapper sijoitteluAjo) {
        SijoittelunTila tila = new SijoittelunTila(sijoitteluAjo);
        SijoitteleKunnesValmisTaiSilmukkaHavaittu sijoittelunIterointi = new SijoitteleKunnesValmisTaiSilmukkaHavaittu();
        try {
            return sijoittelunIterointi.sijoittele(sijoitteluAjo, tila);
        } catch (SijoitteluSilmukkaException s) {
            return SijoitteleKunnesTavoiteHashTuleeVastaanTaiHeitaPoikkeus.sijoittele(
                sijoitteluAjo, tila, sijoittelunIterointi.edellinenHash.get(), sijoittelunIterointi.iteraationHakukohteet
            );
        }
    }

    private static class SijoitteleKunnesTavoiteHashTuleeVastaanTaiHeitaPoikkeus {
        public static SijoittelunTila sijoittele(SijoitteluajoWrapper sijoitteluAjo, SijoittelunTila tila, HashCode tavoiteHash, Set<HakukohdeWrapper> muuttuneetHakukohteet) {
            Set<HashCode> hashset = Sets.newHashSet();

            HashCode hash = sijoitteluAjo.asHash();
            if (hash.equals(tavoiteHash)) {
                LOG.error("###\r\n### Sijoittelu on silmukassa missä yhden iteraation jälkeen päädytään samaan tilaan samoilla muuttuneilla hakukohteilla.\r\n###");
                return tila;
            }
            int i = 0;
            do {
                Set<HakukohdeWrapper> iteraationHakukohteet = muuttuneetHakukohteet;
                muuttuneetHakukohteet = Sets.newHashSet();
                for (HakukohdeWrapper hakukohde : iteraationHakukohteet) {
                    muuttuneetHakukohteet.addAll(SijoitteleHakukohde.sijoitteleHakukohde(sijoitteluAjo, hakukohde));
                }
                hash = sijoitteluAjo.asHash();
                ++i;
                if (hash.equals(tavoiteHash)) {
                    LOG.error("###\r\n### Sijoittelu päätettiin silmukan viimeiseen tilaan. Silmukan koko oli {} iteraatiota.\r\n###", i);
                    return tila;
                }
                if (hashset.contains(hash)) {
                    LOG.error("Sijoittelu on iteraatiolla {} uudelleen aikaisemmassa tilassa (tila {}). Tämä tarkoittaa että sijoittelualgoritmi ei tuota aina samannäköisiä silmukoita.", tila.depth, hash);
                    //throw new SijoitteluFailedException("Sijoittelu on iteraatiolla "+depth+" uudelleen aikaisemmassa tilassa (tila " + hash + ")");
                } else {
                    LOG.info("Iteraatio {} HASH {}", tila.depth, hash);
                    hashset.add(hash);
                }
                ++tila.depth;
            } while (!muuttuneetHakukohteet.isEmpty());
            --tila.depth;
            LOG.error("Sijoittelu meni läpi silmukasta huolimatta. Onko algoritmissa silmukan havaitsemislogiikkaa?");
            return tila;
        }
    }

    private static class SijoitteleKunnesValmisTaiSilmukkaHavaittu {
        private Optional<HashCode> edellinenHash = Optional.empty();
        private SortedSet<HakukohdeWrapper> iteraationHakukohteet;
        public SijoittelunTila sijoittele(SijoitteluajoWrapper sijoitteluAjo, SijoittelunTila tila) {
            final Set<HashCode> hashset = Sets.newHashSet();
            iteraationHakukohteet = new TreeSet<>(new HakukohdeWrapperComparator());
            iteraationHakukohteet.addAll(sijoitteluAjo.getHakukohteet());
            boolean jatkuukoSijoittelu;
            do {
                SortedSet<HakukohdeWrapper> muuttuneetHakukohteet = new TreeSet<>(new HakukohdeWrapperComparator());
                for (HakukohdeWrapper hakukohde : iteraationHakukohteet) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Sijoitellaan hakukohde " + hakukohde.getHakukohde().getOid());
                    }
                    muuttuneetHakukohteet.addAll(SijoitteleHakukohde.sijoitteleHakukohde(sijoitteluAjo, hakukohde));
                }
                HashCode iteraationHash = sijoitteluAjo.asHash();
                ++tila.depth;
                iteraationHakukohteet = muuttuneetHakukohteet;
                jatkuukoSijoittelu = !muuttuneetHakukohteet.isEmpty();
                edellinenHash = Optional.ofNullable(iteraationHash);
                LOG.info("Iteraatio {} HASH {} ja muuttuneet hakukohteet {}", tila.depth, iteraationHash, muuttuneetHakukohteet.size());
                if (jatkuukoSijoittelu && hashset.contains(iteraationHash)) {
                    LOG.error("Sijoittelu on iteraatiolla {} uudelleen aikaisemmassa tilassa (tila {})", tila.depth, iteraationHash);
                    throw new SijoitteluSilmukkaException("Sijoittelu on iteraatiolla " + tila.depth + " uudelleen aikaisemmassa tilassa (tila " + iteraationHash + ")");
                }
                hashset.add(iteraationHash);
            } while (jatkuukoSijoittelu);
            --tila.depth;
            return tila;
        }

    }


}
