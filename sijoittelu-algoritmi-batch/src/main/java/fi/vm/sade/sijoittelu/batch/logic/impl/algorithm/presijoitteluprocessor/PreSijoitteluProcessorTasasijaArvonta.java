package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluFailedException;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;

import java.util.*;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class PreSijoitteluProcessorTasasijaArvonta implements PreSijoitteluProcessor {

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {

        for (HakukohdeWrapper hakukohde : sijoitteluajoWrapper.getHakukohteet()) {
            for (ValintatapajonoWrapper valintatapajono : hakukohde.getValintatapajonot()) {
                Map<Integer, List<HakemusWrapper>> map = new HashMap<Integer, List<HakemusWrapper>>();
                for (HakemusWrapper hakemus : valintatapajono.getHakemukset()) {
                    List<HakemusWrapper> hakemukset = map.get(hakemus.getHakemus().getJonosija());
                    if (hakemukset == null) {
                        hakemukset = new ArrayList<HakemusWrapper>();
                        map.put(hakemus.getHakemus().getJonosija(), hakemukset);
                    }
                    hakemukset.add(hakemus);
                }
                for (List<HakemusWrapper> list : map.values()) {
                    randomize(list);
                }
            }
        }
    }

    private void randomize(List<HakemusWrapper> tasasijaHakemukset) {
        // first check that no tasasija data exists
        // and if it does, it exists for all hakemus
        boolean has = false;
        for (HakemusWrapper hakemus : tasasijaHakemukset) {
            if (hakemus.getHakemus().getTasasijaJonosija() == null) {
                if (has) {
                    throw new SijoitteluFailedException("Partial data on tasasijajonosija for hakemus: "
                            + hakemus.getHakemus().getHakemusOid());
                }
            } else {
                has = true;
            }
        }
        if (!has) {
            // if data does not exist, lets create it
            Collections.shuffle(tasasijaHakemukset);
            for (int i = 0; tasasijaHakemukset.size() > i; i++) {
                tasasijaHakemukset.get(i).getHakemus().setTasasijaJonosija(i + 1);
            }
        }
    }
}