package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;

import java.util.List;
import java.util.stream.Collectors;

public class PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat implements PreSijoitteluProcessor {

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        this.hylkaaHakijaryhmiinKuulumattomat(sijoitteluajoWrapper);

    }

    private void hylkaaHakijaryhmiinKuulumattomat(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.getHakukohteet().forEach(hakukohde -> {
            List<HakijaryhmaWrapper> vainRyhmaanKuuluvatHyvaksytaan = hakukohde
                    .getHakijaryhmaWrappers()
                    .stream()
                    .filter(h -> h.getHakijaryhma().isKaytaKaikki())
                    .collect(Collectors.toList());
            // Hylätään hakijat, jotka eivät kuulu hakijaryhmään
            vainRyhmaanKuuluvatHyvaksytaan.forEach(h -> {
                String jonoOid = h.getHakijaryhma().getValintatapajonoOid();
                if (jonoOid != null) {
                    hakukohde.getValintatapajonot()
                            .stream()
                            .filter(v -> v.getValintatapajono().getOid().equals(jonoOid))
                            .forEach(v -> hylkaaRyhmaanKuulumattomat(h, v));
                } else {
                    hakukohde.getValintatapajonot().forEach(v -> hylkaaRyhmaanKuulumattomat(h, v));
                }
            });
        });

    }

    private void hylkaaRyhmaanKuulumattomat(HakijaryhmaWrapper h, ValintatapajonoWrapper v) {
        v.getHakemukset().forEach(hakemus -> {
            if (!h.getHenkiloWrappers().contains(hakemus.getHenkilo()) && hakemus.isTilaVoidaanVaihtaa()) {
                hakemus.setTilaVoidaanVaihtaa(false);
                hakemus.getHakemus().setTila(HakemuksenTila.HYLATTY);
                hakemus.getHakemus().setTilanKuvaukset(TilanKuvaukset.hylattyHakijaryhmaanKuulumattomana(h.getHakijaryhma().getNimi()));
            }
        });
    }
}
