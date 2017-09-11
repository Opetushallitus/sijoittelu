package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.Timer;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;


public class PostSijoitteluProcessorAsetaSivssnov implements PostSijoitteluProcessor {

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        if (sijoitteluajoWrapper.varasijaSaannotVoimassa()) {
            String hakuOid = sijoitteluajoWrapper.getSijoitteluajo().getHakuOid();

            Timer timer = Timer.start("Post-processor Aseta Sivssnov", "haulle " + hakuOid, PostSijoitteluProcessorAsetaSivssnov.class);

            sijoitteluajoWrapper.getHakukohteet().stream()
                    .flatMap(hkv -> hkv.getValintatapajonot().stream())
                    .map(ValintatapajonoWrapper::getValintatapajono)
                    .forEach(vtj -> vtj.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true));

            timer.stop("Haulle " + hakuOid);
        }
    }
}
