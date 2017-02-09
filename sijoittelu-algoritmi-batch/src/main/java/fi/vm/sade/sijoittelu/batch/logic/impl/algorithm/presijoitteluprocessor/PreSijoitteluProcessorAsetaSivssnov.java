package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;


import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.Timer;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class PreSijoitteluProcessorAsetaSivssnov implements PreSijoitteluProcessor {



    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        if (sijoitteluajoWrapper.isAmkopeHaku() && sijoitteluajoWrapper.varasijaSaannotVoimassa()) {
            String hakuOid = sijoitteluajoWrapper.getSijoitteluajo().getHakuOid();

            Timer timer = Timer.start("Pre-processor Aseta Sivvsnov", "AMKOPE haulle " + hakuOid, PreSijoitteluProcessorAsetaSivssnov.class);

            List<ValintatapajonoWrapper> vtjs = sijoitteluajoWrapper.getHakukohteet().stream().flatMap(hkv ->
                    hkv.getValintatapajonot().stream()).collect(Collectors.toList());

            Boolean kaikkiaJonojaEiOleSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa = vtjs.stream().anyMatch(x -> {
                Boolean lippu = x.getValintatapajono().getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa();
                return (lippu == null || lippu == false);
            });

            if (kaikkiaJonojaEiOleSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa) {
                vtjs.stream().map(ValintatapajonoWrapper::getValintatapajono).forEach(vtj ->
                        vtj.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(false));
            }

            timer.stop("AMKOPE haulle " + hakuOid);
        }
    }
}
