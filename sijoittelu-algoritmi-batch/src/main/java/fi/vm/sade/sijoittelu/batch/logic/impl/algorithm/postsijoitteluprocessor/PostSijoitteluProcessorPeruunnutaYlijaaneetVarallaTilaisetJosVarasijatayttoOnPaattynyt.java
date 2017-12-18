package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.PERUUNTUNUT;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class PostSijoitteluProcessorPeruunnutaYlijaaneetVarallaTilaisetJosVarasijatayttoOnPaattynyt implements PostSijoitteluProcessor {
    private Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorPeruunnutaYlijaaneetVarallaTilaisetJosVarasijatayttoOnPaattynyt.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        if (sijoitteluajoWrapper.varasijaSaannotVoimassa()) {
            LOG.info(String.format("Varasijasäännöt ovat voimassa, varmistetaan että %s-tilaiset muutetaan tilaan %s " +
                "kaikille jonoille, joiden varasijatäyttö on päättynyt", VARALLA, PERUUNTUNUT));
            List<ValintatapajonoWrapper> kasiteltavatJonot = sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(hkv -> hkv.getValintatapajonot().stream())
                .filter(sijoitteluajoWrapper::onkoVarasijaSaannotVoimassaJaVarasijaTayttoPaattynyt)
                .collect(Collectors.toList());

            kasiteltavatJonot.stream().flatMap(jonoWrapper -> jonoWrapper.getHakemukset().stream())
                .filter(HakemusWrapper::isVaralla)
                .forEach(hw -> {
                    if (hw.isTilaVoidaanVaihtaa()) {
                        LOG.info(String.format("(AjoId: %s ) PostProcessor: Asetetaan %s oleva hakemus %s tilaan %s",
                            sijoitteluajoWrapper.getSijoitteluAjoId(), VARALLA, hw.getHakemus().getHakemusOid(), PERUUNTUNUT));
                        TilojenMuokkaus.asetaTilaksiPeruuntunutHakukierrosPaattynyt(hw);
                    } else {
                        LOG.info(String.format("(AjoId: %s ) PostProcessor: Oltaisiin haluttu asettaa %s oleva hakemus %s tilaan %s, " +
                            "mutta sen tila ei ole jostain syystä muokattavissa",
                            sijoitteluajoWrapper.getSijoitteluAjoId(), VARALLA, hw.getHakemus().getHakemusOid(), PERUUNTUNUT));
                    }
                });
        }
    }
}
