package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;


import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.Timer;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;

import java.util.List;
import java.util.stream.Collectors;

public class PreSijoitteluProcessorAsetaSivssnov implements PreSijoitteluProcessor {

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        if (sijoitteluajoWrapper.isAmkopeHaku() && sijoitteluajoWrapper.varasijaSaannotVoimassa()) {
            String hakuOid = sijoitteluajoWrapper.getSijoitteluajo().getHakuOid();

            Timer timer = Timer.start("Pre-processor Aseta Sivssnov", "AMKOPE haulle " + hakuOid, PreSijoitteluProcessorAsetaSivssnov.class);

            List<Valintatapajono> vtjs = sijoitteluajoWrapper.getHakukohteet().stream()
                    .flatMap(hkv -> hkv.getValintatapajonot().stream())
                    .map(ValintatapajonoWrapper::getValintatapajono)
                    .collect(Collectors.toList());

            boolean kaikkiaJonojaEiOleSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa = vtjs.stream()
                    .anyMatch(vtj -> !vtj.getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa());

            if (kaikkiaJonojaEiOleSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa) {
                vtjs.forEach(vtj -> vtj.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(false));
            }

            timer.stop("AMKOPE haulle " + hakuOid);
        }
    }
}
