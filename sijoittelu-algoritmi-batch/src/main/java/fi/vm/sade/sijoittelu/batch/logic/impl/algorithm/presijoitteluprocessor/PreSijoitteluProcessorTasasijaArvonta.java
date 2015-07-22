package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;

public class PreSijoitteluProcessorTasasijaArvonta implements PreSijoitteluProcessor {
    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {

        for (HakukohdeWrapper hakukohde : sijoitteluajoWrapper.getHakukohteet()) {
            for (ValintatapajonoWrapper valintatapajono : hakukohde.getValintatapajonot()) {
                Map<Integer, List<HakemusWrapper>> map = new ConcurrentHashMap<>();
                for (HakemusWrapper hakemus : valintatapajono.getHakemukset()) {
                    map.putIfAbsent(hakemus.getHakemus().getJonosija(), new ArrayList<>());
                    List<HakemusWrapper> hakemukset = map.get(hakemus.getHakemus().getJonosija());
                    hakemukset.add(hakemus);
                }
                for (List<HakemusWrapper> list : map.values()) {
                    randomize(list);
                }
            }
        }
    }

    private void randomize(List<HakemusWrapper> tasasijaHakemukset) {
        Collections.shuffle(tasasijaHakemukset);
        List<HakemusWrapper> jonosija = new ArrayList<HakemusWrapper>();
        for (HakemusWrapper hakemusWrapper : tasasijaHakemukset) {
            if (hakemusWrapper.getHakemus().getTasasijaJonosija() != null) {
                jonosija.add(hakemusWrapper);
            }
        }
        Collections.sort(jonosija, new Comparator<HakemusWrapper>() {
            @Override
            public int compare(HakemusWrapper hakemusWrapper, HakemusWrapper hakemusWrapper2) {
                return hakemusWrapper.getHakemus().getTasasijaJonosija().compareTo(hakemusWrapper2.getHakemus().getTasasijaJonosija());
            }
        });
        for (int i = 0; tasasijaHakemukset.size() > i; i++) {
            if (tasasijaHakemukset.get(i).getHakemus().getTasasijaJonosija() != null) {
                tasasijaHakemukset.remove(i);
                tasasijaHakemukset.add(i, jonosija.remove(0));
            }
            tasasijaHakemukset.get(i).getHakemus().setTasasijaJonosija(i + 1);
        }
    }
}