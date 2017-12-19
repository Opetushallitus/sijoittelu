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
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        this.hylkaaHakijaryhmiinKuulumattomat(sijoitteluajoWrapper);
    }

    private void hylkaaHakijaryhmiinKuulumattomat(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.getHakukohteet().forEach(hakukohde -> {
            List<HakijaryhmaWrapper> ryhmatJoistaHyvaksytaanKaikki = hakukohde
                .getHakijaryhmaWrappers()
                .stream()
                .filter(h -> h.getHakijaryhma().isKaytaKaikki())
                .collect(Collectors.toList());
            if (ryhmatJoistaHyvaksytaanKaikki.size() > 1) {
                throw new IllegalStateException(String.format("(Sijoitteluajo %s) Hakukohteelle %s on olemassa useampia kuin yksi hakijaryhmä, " +
                    "josta kaikki ryhmään kuuluvat olisi tarkoitus hyväksyä. Sijoittelua ei voida suorittaa.",
                    sijoitteluajoWrapper.getSijoitteluAjoId(),
                    hakukohde.getHakukohde().getOid()));
            }
            // Hylätään hakijat, jotka eivät kuulu hakijaryhmään
            ryhmatJoistaHyvaksytaanKaikki.forEach(ryhmaWrapper -> {
                String jonoOid = ryhmaWrapper.getHakijaryhma().getValintatapajonoOid();
                if (jonoOid != null) {
                    hakukohde.getValintatapajonot()
                        .stream()
                        .filter(v -> v.getValintatapajono().getOid().equals(jonoOid))
                        .forEach(v -> hylkaaRyhmaanKuulumattomat(ryhmaWrapper, v));
                } else {
                    hakukohde.getValintatapajonot().forEach(v -> hylkaaRyhmaanKuulumattomat(ryhmaWrapper, v));
                }
            });
        });
    }

    private void hylkaaRyhmaanKuulumattomat(HakijaryhmaWrapper ryhmaWrapper, ValintatapajonoWrapper jonoWrapper) {
        jonoWrapper.getHakemukset().forEach(hakemus -> {
            if (!ryhmaWrapper.getHenkiloWrappers().contains(hakemus.getHenkilo()) && hakemus.isTilaVoidaanVaihtaa()) {
                hakemus.getHakemus().setTila(HakemuksenTila.HYLATTY);
                hakemus.getHakemus().setTilanKuvaukset(TilanKuvaukset.hylattyHakijaryhmaanKuulumattomana(ryhmaWrapper.getHakijaryhma().getNimi()));
                hakemus.setTilaVoidaanVaihtaa(false);
            }
        });
    }
}
