package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PreSijoitteluProcessorTarkistaHakutoiveidenPrioriteettienEheys implements PreSijoitteluProcessor {
    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.kaikkiHakijat().forEach(henkiloWrapper -> {
            Map<String, List<HakemusWrapper>> hakemuksenTuloksetByHakukohdeOid = henkiloWrapper.getHakemukset().stream()
                .collect(Collectors.groupingBy(HakemusWrapper::getHakukohdeOid));
            hakemuksenTuloksetByHakukohdeOid.forEach(this::tarkistaEttaTuloksissaOnSamaHakutoivePrioriteetti);
        });
    }

    private void tarkistaEttaTuloksissaOnSamaHakutoivePrioriteetti(String hakukohdeOid, List<HakemusWrapper> samanToiveenTulokset) {
        for (HakemusWrapper hakemusWrapper : samanToiveenTulokset) {
            if (prioriteeteissaOnRistiriita(samanToiveenTulokset, hakemusWrapper)) {
                List<String> jonoOiditJaPrioriteetit = samanToiveenTulokset.stream()
                    .map(hw -> hw.getValintatapajono().getValintatapajono().getOid() + " : " + hw.getHakemus().getPrioriteetti())
                    .collect(Collectors.toList());
                throw new IllegalStateException(String.format("Hakukohteessa %s hakemuksella %s on eri prioriteetteja! %s",
                    hakukohdeOid, hakemusWrapper.getHakemus().getHakemusOid(), jonoOiditJaPrioriteetit));
            }
        }
    }

    private boolean prioriteeteissaOnRistiriita(List<HakemusWrapper> samanToiveenTulokset, HakemusWrapper hakemusWrapper) {
        return !Objects.equals(hakemusWrapper.getHakemus().getPrioriteetti(), samanToiveenTulokset.get(0).getHakemus().getPrioriteetti());
    }
}
