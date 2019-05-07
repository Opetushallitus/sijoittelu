package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;


import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PreSijoitteluProcessorAsetaSivssnov implements PreSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PreSijoitteluProcessorAsetaSivssnov.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        if (!sijoitteluajoWrapper.varasijaSaannotVoimassa() &&
            LocalDateTime.now().isBefore(sijoitteluajoWrapper.getVarasijaSaannotAstuvatVoimaan())) {
            List<Valintatapajono> sivssnovJonot = sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(hkv -> hkv.getValintatapajonot().stream())
                .map(ValintatapajonoWrapper::getValintatapajono)
                .filter(Valintatapajono::getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa)
                .collect(Collectors.toList());
            if (!sivssnovJonot.isEmpty()) {
                LOG.warn(String.format("Haun %s Varasijasäännöt eivät ole vielä voimassa, mutta sillä on %d jonoa, " +
                        "joilla SIVSSNOV-lippu on päällä. Käännetään liput pois jonoilta %s.", sijoitteluajoWrapper.getSijoitteluajo().getHakuOid(),
                    sivssnovJonot.size(), sivssnovJonot.stream().map(Valintatapajono::getOid).collect(Collectors.joining(", "))));
                sivssnovJonot.forEach(j -> {
                    j.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(false);
                    j.setSivssnovSijoittelunVarasijataytonRajoitus(Optional.empty());
                });
            }
        }
    }
}
