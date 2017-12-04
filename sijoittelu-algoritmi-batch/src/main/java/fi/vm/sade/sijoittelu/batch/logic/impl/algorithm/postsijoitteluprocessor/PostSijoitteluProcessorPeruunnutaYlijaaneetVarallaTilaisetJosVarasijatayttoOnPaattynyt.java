package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class PostSijoitteluProcessorPeruunnutaYlijaaneetVarallaTilaisetJosVarasijatayttoOnPaattynyt implements  PostSijoitteluProcessor {
    Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorPeruunnutaYlijaaneetVarallaTilaisetJosVarasijatayttoOnPaattynyt.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {

        if(sijoitteluajoWrapper.varasijaSaannotVoimassa()) {
            LOG.info("Varasijasäännöt ovat voimassa, varmistetaan että VARALLA-tilaiset muutetaan tilaan PERUUNTUNUT kaikille jonoille, joiden varasijatäyttö on päättynyt");
            List<ValintatapajonoWrapper> kasiteltavatJonot = sijoitteluajoWrapper.getHakukohteet().stream()
                    .flatMap(hkv -> hkv.getValintatapajonot().stream())
                    .filter(sijoitteluajoWrapper::onkoVarasijaSaannotVoimassaJaVarasijaTayttoPaattynyt)
                    .collect(Collectors.toList());

            kasiteltavatJonot.stream().flatMap(jonoWrapper -> jonoWrapper.getHakemukset().stream())
                    .filter(HakemusWrapper::isVaralla)
                    .forEach(hw -> {
                        if(hw.isTilaVoidaanVaihtaa()) {
                            LOG.info("(AjoId: {} ) PostProcessor: Asetetaan VARALLA oleva hakemus {} tilaan PERUUNTUNUT",
                                    sijoitteluajoWrapper.getSijoitteluajo().getSijoitteluajoId(), hw.getHakemus().getHakemusOid());
                            TilojenMuokkaus.asetaTilaksiPeruuntunutHakukierrosPaattynyt(hw);
                        } else {
                            LOG.info("(AjoId: {} ) PostProcessor: Oltaisiin haluttu asettaa VARALLA oleva hakemus {} tilaan PERUUNTUNUT, mutta sen tila ei ole jostain syystä muokattavissa",
                                    sijoitteluajoWrapper.getSijoitteluajo().getSijoitteluajoId(), hw.getHakemus().getHakemusOid());
                        }
                    });
        }
    }
}
