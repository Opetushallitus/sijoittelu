package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessorMuutostiedonAsetus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorLahtotilanteenHash;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorSort;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorTasasijaArvonta;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.*;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods.*;

public abstract class SijoitteluAlgorithm {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluAlgorithm.class);

    private final static List<PreSijoitteluProcessor> preSijoitteluProcessors = Arrays.asList(
        new PreSijoitteluProcessorTasasijaArvonta(),
        new PreSijoitteluProcessorSort(),
        new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt(),
        new PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat(),
        new PreSijoitteluProcessorLahtotilanteenHash()
    );
    private final static List<PostSijoitteluProcessor> postSijoitteluProcessors = Arrays.asList(
        new PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus(),
        new PostSijoitteluProcessorMuutostiedonAsetus()
    );

    public static SijoittelunTila sijoittele(SijoitteluAjo sijoitteluAjo, List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset) {
        return sijoittele(SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(sijoitteluAjo, hakukohteet, valintatulokset));
    }

    public static SijoittelunTila sijoittele(SijoitteluajoWrapper sijoitteluAjo) {
        LOG.info("(hakuOid={}) Starting sijoitteluajo {}",
                Optional.ofNullable(sijoitteluAjo.getSijoitteluajo()).orElse(new SijoitteluAjo()).getHakuOid(), sijoitteluAjo.getSijoitteluAjoId());
        runPreProcessors(sijoitteluAjo);
        final SijoittelunTila tila = suoritaSijoittelu(sijoitteluAjo);
        runPostProcessors(sijoitteluAjo);
        return tila;
    }

    private static void runPreProcessors(final SijoitteluajoWrapper sijoitteluAjo) {
        for (PreSijoitteluProcessor processor : preSijoitteluProcessors) {
            LOG.info("(hakuOid={}) Starting preprocessor {} for sijoitteluAjo {}",
                    Optional.ofNullable(sijoitteluAjo.getSijoitteluajo()).orElse(new SijoitteluAjo()).getHakuOid(), processor.name(), sijoitteluAjo.getSijoitteluAjoId());
            processor.process(sijoitteluAjo);
        }
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

    private static void runPostProcessors(final SijoitteluajoWrapper sijoitteluAjo) {
        for (PostSijoitteluProcessor processor : postSijoitteluProcessors) {
            LOG.info("(hakuOid={}) Starting postprocessor {} for sijoitteluAjo {}",
                    Optional.ofNullable(sijoitteluAjo.getSijoitteluajo()).orElse(new SijoitteluAjo()).getHakuOid(), processor.name(), sijoitteluAjo.getSijoitteluAjoId());
            processor.process(sijoitteluAjo);
        }
    }

    private static class SijoitteleKunnesTavoiteHashTuleeVastaanTaiHeitaPoikkeus {
        public static SijoittelunTila sijoittele(SijoitteluajoWrapper sijoitteluAjo, SijoittelunTila tila, HashCode tavoiteHash, Set<HakukohdeWrapper> muuttuneetHakukohteet) {
            Set<HashCode> hashset = Sets.newHashSet();

            HashCode hash = sijoitteluAjo.asHash();
            if (hash.equals(tavoiteHash)) {
                LOG.error("###\r\n### Sijoittelu on silmukassa missä yhden iteraation jälkeen päädytään samaan tilaan samoilla muuttuneilla hakukohteilla.\r\n###");
                //return;
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
                    //return;
                }
                if (hashset.contains(hash)) {
                    LOG.error("Sijoittelu on iteraatiolla {} uudelleen aikaisemmassa tilassa (tila {}). Tämä tarkoittaa että sijoittelualgoritmi ei tuota aina samannäköisiä silmukoita.", tila.depth, hash);
                    //throw new SijoitteluFailedException("Sijoittelu on iteraatiolla "+depth+" uudelleen aikaisemmassa tilassa (tila " + hash + ")");
                } else {
                    LOG.debug("Iteraatio {} HASH {}", tila.depth, hash);
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
        private Set<HakukohdeWrapper> iteraationHakukohteet;
        public SijoittelunTila sijoittele(SijoitteluajoWrapper sijoitteluAjo, SijoittelunTila tila) {
            final Set<HashCode> hashset = Sets.newHashSet();
            iteraationHakukohteet = Sets.newHashSet(sijoitteluAjo.getHakukohteet());
            boolean jatkuukoSijoittelu;
            do {
                Set<HakukohdeWrapper> muuttuneetHakukohteet = Sets.newHashSet();
                for (HakukohdeWrapper hakukohde : iteraationHakukohteet) {
                    muuttuneetHakukohteet.addAll(SijoitteleHakukohde.sijoitteleHakukohde(sijoitteluAjo, hakukohde));
                }
                HashCode iteraationHash = sijoitteluAjo.asHash();
                ++tila.depth;
                iteraationHakukohteet = muuttuneetHakukohteet;
                jatkuukoSijoittelu = !muuttuneetHakukohteet.isEmpty();
                edellinenHash = Optional.ofNullable(iteraationHash);
                LOG.debug("Iteraatio {} HASH {} ja muuttuneet hakukohteet {}", tila.depth, iteraationHash, muuttuneetHakukohteet.size());
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
