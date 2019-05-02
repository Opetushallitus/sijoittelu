package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PostSijoitteluProcessorAsetaSivssnov implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorAsetaSivssnov.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        if (sijoitteluajoWrapper.varasijaSaannotVoimassa()) {
            String hakuOid = sijoitteluajoWrapper.getSijoitteluajo().getHakuOid();

            sijoitteluajoWrapper.getHakukohteet().stream()
                    .flatMap(hkv -> hkv.getValintatapajonot().stream())
                    .map(ValintatapajonoWrapper::getValintatapajono)
                    .forEach(vtj -> vtj.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true));

            LOG.info(String.format("Asetettiin \"sijoiteltu ilman varasijasääntöjä niiden ollessa voimassa\" haun %s valintatapajonoille.", hakuOid));
        }
    }
}
