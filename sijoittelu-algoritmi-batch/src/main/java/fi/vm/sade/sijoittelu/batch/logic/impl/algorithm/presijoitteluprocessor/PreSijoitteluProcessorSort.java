package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakijaryhmaWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakukohdeWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.ValintatapajonoWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PreSijoitteluProcessorSort implements PreSijoitteluProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(PreSijoitteluProcessorSort.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        this.sortDomain(sijoitteluajoWrapper);

    }

    /**
     * jarjestelee sijoittelun domainin. Ilman tata algoritmin toimintaan ei voi
     * luottaaa (XML data voi olla vaarassa jarjestyksessa)
     */
    private void sortDomain(SijoitteluajoWrapper sijoitteluajoWrapper) {
        for (HakukohdeWrapper hakukohde : sijoitteluajoWrapper.getHakukohteet()) {
            for (ValintatapajonoWrapper valintatapajonoWrapper : hakukohde.getValintatapajonot()) {
                Collections.sort(valintatapajonoWrapper.getHakemukset(), new HakemusWrapperComparator());
            }
            Collections.sort(hakukohde.getValintatapajonot(), new ValintatapajonoWrapperComparator());

            List<HakijaryhmaWrapper> hakijaryhmat = hakukohde.getHakijaryhmaWrappers().stream()
                    .filter(hr -> StringUtils.isBlank(hr.getHakijaryhma().getValintatapajonoOid()))
                    .sorted(new HakijaryhmaWrapperComparator())
                    .collect(Collectors.toList());
            Map<String, List<HakijaryhmaWrapper>> valintatapajonojenHakijaryhmat = hakukohde.getHakijaryhmaWrappers().stream()
                    .filter(hr -> StringUtils.isNotBlank(hr.getHakijaryhma().getValintatapajonoOid()))
                    .collect(Collectors.groupingBy(
                        hr -> hr.getHakijaryhma().getValintatapajonoOid()
                    ));
            hakukohde.getValintatapajonot().stream().forEachOrdered(jono -> {
                if(valintatapajonojenHakijaryhmat.containsKey(jono.getValintatapajono().getOid())) {
                    hakijaryhmat.addAll(
                            valintatapajonojenHakijaryhmat.get(jono.getValintatapajono().getOid()).stream()
                                    .sorted(new HakijaryhmaWrapperComparator())
                                    .collect(Collectors.toList())
                    );
                }
            });
            hakukohde.setHakijaryhmaWrappers(hakijaryhmat);
        }
        Collections.sort(sijoitteluajoWrapper.getHakukohteet(), new HakukohdeWrapperComparator());
    }
}
