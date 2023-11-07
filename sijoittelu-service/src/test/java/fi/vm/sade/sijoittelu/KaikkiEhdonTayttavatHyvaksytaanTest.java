package fi.vm.sade.sijoittelu;


import fi.vm.sade.testing.TestConfigurationWithMocks;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ContextConfiguration(classes = {TestConfigurationWithMocks.class})
@ExtendWith(SpringExtension.class)
public class KaikkiEhdonTayttavatHyvaksytaanTest {

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

	@Test
    //@UsingDataSet(locations = "kaikki_ehdon_tayttavat_hyvaksytaan.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void testKaikkiEhdonTayttavatHyvaksytaan(TestInfo testInfo) throws IOException {
        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), java.util.Collections.emptyMap());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        List<String> hyvaksyttavat = Arrays.asList(
                "1.2.246.562.11.00001164796",
                "1.2.246.562.11.00001098671",
                "1.2.246.562.11.00001123782",
                "1.2.246.562.11.00001237717",
                "1.2.246.562.11.00001256976",
                "1.2.246.562.11.00001104028",
                "1.2.246.562.11.00001119068",
                "1.2.246.562.11.00001188044",
                "1.2.246.562.11.00001265857",
                "1.2.246.562.11.00001040263",
                "1.2.246.562.11.00001082690",
                "1.2.246.562.11.00001109421",
                "1.2.246.562.11.00001262630",
                "1.2.246.562.11.00001155569",
                "1.2.246.562.11.00001230109",
                "1.2.246.562.11.00001191578",
                "1.2.246.562.11.00001130432",
                "1.2.246.562.11.00001014707",
                "1.2.246.562.11.00001178009",
                "1.2.246.562.11.00001095506",
                "1.2.246.562.11.00001125395",
                "1.2.246.562.11.00001187553",
                "1.2.246.562.11.00001206515",
                "1.2.246.562.11.00001237856",
                "1.2.246.562.11.00001044528",
                "1.2.246.562.11.00001031502",
                "1.2.246.562.11.00001126352");

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), hyvaksyttavat);

    }

    public final static void assertoiAinoastaanValittu(Valintatapajono h, List<String> wanted) {
        List<String> actual = new ArrayList<String>();
        for (Hakemus hakemus : h.getHakemukset()) {
            if (hakemus.getTila() == HakemuksenTila.HYVAKSYTTY) {
                actual.add(hakemus.getHakemusOid());
            }
        }
        Assertions.assertTrue(actual.containsAll(wanted), "Actual result does not contain all wanted approved OIDs");
        Assertions.assertTrue(wanted.containsAll(actual), "Wanted result contains more approved OIDs than actual");
    }

}
