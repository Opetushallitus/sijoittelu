package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.Timer;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;

import java.util.List;
import java.util.stream.Collectors;


public class PostSijoitteluProcessorAsetaSivssnov implements PostSijoitteluProcessor {

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        if (sijoitteluajoWrapper.isAmkopeHaku() && sijoitteluajoWrapper.varasijaSaannotVoimassa()) {
            String hakuOid = sijoitteluajoWrapper.getSijoitteluajo().getHakuOid();

            Timer timer = Timer.start("Post-processor Aseta Sivvsnov", "AMKOPE haulle " + hakuOid, PostSijoitteluProcessorAsetaSivssnov.class);
            List<ValintatapajonoWrapper> vtjs = sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(hkv -> hkv.getValintatapajonot().stream()).collect(Collectors.toList());

            vtjs.stream()
                .map(ValintatapajonoWrapper::getValintatapajono)
                .forEach(vtj -> vtj.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true));

            timer.stop("AMKOPE haulle " + hakuOid);
        }
    }
}
