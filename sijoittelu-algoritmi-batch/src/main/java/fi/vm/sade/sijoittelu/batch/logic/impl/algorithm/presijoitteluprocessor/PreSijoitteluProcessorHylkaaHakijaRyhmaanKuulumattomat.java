package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakijaryhma;

import java.util.List;
import java.util.stream.Collectors;

public class PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat implements PreSijoitteluProcessor {

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        this.hylkaaHakijaryhmiinKuulumattomat(sijoitteluajoWrapper);
    }

    private void hylkaaHakijaryhmiinKuulumattomat(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.getHakukohteet().forEach(hakukohde -> {
            List<HakijaryhmaWrapper> ryhmatJoistaVainKuuluvatHyvaksytaan = hakukohde
                .getHakijaryhmaWrappers()
                .stream()
                .filter(h -> h.getHakijaryhma().isKaytaKaikki())
                .collect(Collectors.toList());
            if (ryhmatJoistaVainKuuluvatHyvaksytaan.size() > 1) {
                List<String> ryhmienOiditJaNimet = ryhmatJoistaVainKuuluvatHyvaksytaan.stream().map(hjr ->
                {
                    Hakijaryhma hakijaryhma = hjr.getHakijaryhma();
                    return String.format("%s (\"%s\"), hakukohdeOid= %s , valintatapajonoOid = %s .",
                        hakijaryhma.getOid(),
                        hakijaryhma.getNimi(),
                        hakijaryhma.getHakukohdeOid(),
                        hakijaryhma.getValintatapajonoOid());
                }).collect(Collectors.toList());
                throw new IllegalStateException(String.format("(Sijoitteluajo %s) Hakukohteelle %s on olemassa useampi kuin yksi hakijaryhmä, " +
                    "josta vain ryhmään kuuluvat olisi tarkoitus hyväksyä. Sijoittelua ei voida suorittaa. Ryhmät: %s",
                    sijoitteluajoWrapper.getSijoitteluAjoId(),
                    hakukohde.getHakukohde().getOid(),
                    ryhmienOiditJaNimet));
            }
            // Hylätään hakijat, jotka eivät kuulu hakijaryhmään
            ryhmatJoistaVainKuuluvatHyvaksytaan.forEach(ryhmaWrapper -> {
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
