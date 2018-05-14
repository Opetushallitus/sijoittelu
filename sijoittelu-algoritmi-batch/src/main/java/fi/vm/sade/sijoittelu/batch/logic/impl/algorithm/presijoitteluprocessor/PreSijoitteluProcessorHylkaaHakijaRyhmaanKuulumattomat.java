package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import static fi.vm.sade.sijoittelu.domain.TilanKuvaukset.hylattyHakijaryhmaanKuulumattomana;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYLATTY;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakijaryhma;

import java.util.List;
import java.util.stream.Collectors;

public class PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat implements PreSijoitteluProcessor {

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        this.hylkaaHakijaryhmiinKuulumattomat(sijoitteluajoWrapper);
    }

    private void hylkaaHakijaryhmiinKuulumattomat(SijoitteluajoWrapper sijoitteluajoWrapper) {
        assertKorkeintaanYksiRyhmaJostaVainRyhmaanKuuluvatHyvaksytaanPerHakukohde(sijoitteluajoWrapper);

        sijoitteluajoWrapper.getHakukohteet().forEach(hakukohde -> {
            List<HakijaryhmaWrapper> ryhmatJoistaVainKuuluvatHyvaksytaan = hakukohde
                .getHakijaryhmaWrappers()
                .stream()
                .filter(h -> h.getHakijaryhma().isKaytaKaikki())
                .collect(Collectors.toList());
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
                hakemus.getHakemus().setTila(HYLATTY);
                hakemus.getHakemus().setTilanKuvaukset(
                    hylattyHakijaryhmaanKuulumattomana(ryhmaWrapper.getHakijaryhma().getNimi()));
                hakemus.setTilaVoidaanVaihtaa(false);
            }
        });
    }

    private void assertKorkeintaanYksiRyhmaJostaVainRyhmaanKuuluvatHyvaksytaanPerHakukohde(SijoitteluajoWrapper sijoitteluajoWrapper) {
        List<HakukohdeWrapper> hakukohteetJoissaOnUseampiRyhmaJoistaVainKuuluvatHyvaksytaan =
            sijoitteluajoWrapper.getHakukohteet().stream().filter(hkw ->
                hkw.getHakijaryhmaWrappers().stream().filter(hrw ->
                    hrw.getHakijaryhma().isKaytaKaikki()).count() > 1)
                .collect(Collectors.toList());

        if (!hakukohteetJoissaOnUseampiRyhmaJoistaVainKuuluvatHyvaksytaan.isEmpty()) {
            List<String> ryhmienOiditJaNimet = hakukohteetJoissaOnUseampiRyhmaJoistaVainKuuluvatHyvaksytaan.stream().flatMap(hkw ->
                hkw.getHakijaryhmaWrappers().stream()).map(hjr -> {
                Hakijaryhma hakijaryhma = hjr.getHakijaryhma();
                return String.format("%s (\"%s\"), hakukohdeOid = %s , valintatapajonoOid = %s .",
                    hakijaryhma.getOid(),
                    hakijaryhma.getNimi(),
                    hakijaryhma.getHakukohdeOid(),
                    hakijaryhma.getValintatapajonoOid());
            }).collect(Collectors.toList());
            throw new IllegalStateException(
                String.format("(Sijoitteluajo %s) %s hakukohteelle on olemassa useampi kuin yksi hakijaryhmä, " +
                        "josta vain ryhmään kuuluvat olisi tarkoitus hyväksyä. Sijoittelua ei voida suorittaa. Ryhmät: %s",
                    sijoitteluajoWrapper.getSijoitteluAjoId(),
                    hakukohteetJoissaOnUseampiRyhmaJoistaVainKuuluvatHyvaksytaan.size(),
                    ryhmienOiditJaNimet));
        }
    }
}
