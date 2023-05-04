package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.*;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;

import java.math.BigDecimal;
import java.util.*;

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
        SijoitteluajoWrapper wrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), sijoitteluAjo, hakukohteet,
                Collections.emptyMap());
        wrapper.paivitaVastaanottojenVaikutusHakemustenTiloihin(valintatulokset, aiemmanVastaanotonHakukohdePerHakija);
        wrapper.setKKHaku(true);
        return SijoitteluAlgorithm.sijoittele(preProcessors, postProcessors, wrapper);
    }

    public static SijoittelunTila sijoittele(SijoitteluajoWrapper ajo) {
        return SijoitteluAlgorithm.sijoittele(PreSijoitteluProcessor.defaultPreProcessors(),
            PostSijoitteluProcessor.defaultPostProcessors(),
            ajo);
    }

    public static List<Hakemus> generateHakemukset(int nToGenerate, int startingJonosija, Hakijaryhma hakijaryhma) {
        List<Hakemus> results = new ArrayList<>(nToGenerate);
        for (int i = startingJonosija; i < startingJonosija+nToGenerate; i++) {
            results.add(generateHakemus(i, hakijaryhma));
        }
        return results;
    }

    public static List<Hakemus> generateHakemukset(int nToGenerate, Hakijaryhma hakijaryhma) {
        List<Hakemus> results = new ArrayList<>(nToGenerate);
        for (int i = 0; i < nToGenerate; i++) {
            results.add(generateHakemus(i, hakijaryhma));
        }
        return results;
    }

    private static Hakemus generateHakemus(int i, Hakijaryhma hakijaryhma) {
        return generateHakemus(i, i, hakijaryhma);
    }

    public static Hakemus generateHakemus(int i, int jonosija, Hakijaryhma hakijaryhma) {
        Hakemus h = new Hakemus();
        h.setJonosija(jonosija);
        h.setPrioriteetti(0);
        h.setHakemusOid("hakemus" + i);
        h.setHakijaOid("hakija" + i);
        if (hakijaryhma != null) {
            hakijaryhma.getHakemusOid().add(h.getHakemusOid());
        }
        h.setPisteet(new BigDecimal(i));
        TilojenMuokkaus.asetaTilaksiVaralla(h);
        return h;
    }
}
