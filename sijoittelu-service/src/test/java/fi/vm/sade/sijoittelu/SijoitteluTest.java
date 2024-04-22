package fi.vm.sade.sijoittelu;

import com.google.common.collect.ImmutableList;

import fi.vm.sade.testing.AbstractIntegrationTest;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
public class SijoitteluTest extends AbstractIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluTest.class);

    @Test
    public void syksy2013Test() {
        List<Hakukohde> hakukohteet = TestHelper.readHakukohteetListFromJson("sijoittelutestdata/sijoittelutestidata.json");
        Map<String, List<Hakemus>> hakemusMapByHakemusOid = new HashMap<String, List<Hakemus>>();

        for (Hakukohde hk : hakukohteet) {
            for (Valintatapajono vt : hk.getValintatapajonot()) {
                for (Hakemus h : vt.getHakemukset()) {

                    if (hakemusMapByHakemusOid.get(h.getHakemusOid()) != null) {
                        List<Hakemus> list = hakemusMapByHakemusOid.get(h.getHakemusOid());
                        list.add(h);
                    } else {
                        List<Hakemus> list = new ArrayList<Hakemus>();
                        list.add(h);
                        hakemusMapByHakemusOid.put(h.getHakemusOid(), list);
                    }

                    if (h.getTila() != HakemuksenTila.HYLATTY || h.getTila() != HakemuksenTila.PERUNUT) {
                        h.setTila(HakemuksenTila.VARALLA);
                    }
                }
            }
        }
        SijoitteluAlgorithmUtil.sijoittele(hakukohteet, ImmutableList.of(), Collections.emptyMap());
        ass(hakemusMapByHakemusOid, "1.2.246.562.11.00000011992", 1, getHakukohde(hakukohteet, "1.2.246.562.14.2013082908162538927436"), HakemuksenTila.HYVAKSYTTY);
        ass(hakemusMapByHakemusOid, "1.2.246.562.11.00000011992", 2, getHakukohde(hakukohteet, "1.2.246.562.5.02563_04_873_0530"), HakemuksenTila.PERUUNTUNUT);

        // System.out.println PrintHelper.tulostaSijoittelu(sijoitteluAlgorithm));
    }

    @Test
    public void syksy2013HarkinnanvaraisestiHyvaksytytTest() {
        List<Hakukohde> hakukohteet = TestHelper.readHakukohteetListFromJson("sijoittelutestdata/sijoittelutestidata.json");
        Map<String, List<Hakemus>> hakemusMapByHakemusOid = new HashMap<String, List<Hakemus>>();

        for (Hakukohde hk : hakukohteet) {
            for (Valintatapajono vt : hk.getValintatapajonot()) {
                for (Hakemus h : vt.getHakemukset()) {

                    if (hakemusMapByHakemusOid.get(h.getHakemusOid()) != null) {
                        List<Hakemus> list = hakemusMapByHakemusOid.get(h.getHakemusOid());
                        list.add(h);
                    } else {
                        List<Hakemus> list = new ArrayList<Hakemus>();
                        list.add(h);
                        hakemusMapByHakemusOid.put(h.getHakemusOid(), list);
                    }
                }
            }
        }

        ass(hakemusMapByHakemusOid, "1.2.246.562.11.00000011992", 1, getHakukohde(hakukohteet, "1.2.246.562.14.2013082908162538927436"), HakemuksenTila.HYVAKSYTTY);
        ass(hakemusMapByHakemusOid, "1.2.246.562.11.00000011992", 2, getHakukohde(hakukohteet, "1.2.246.562.5.02563_04_873_0530"), HakemuksenTila.VARALLA);
        SijoitteluAlgorithmUtil.sijoittele(hakukohteet, ImmutableList.of(), Collections.emptyMap());
        ass(hakemusMapByHakemusOid, "1.2.246.562.11.00000011992", 1, getHakukohde(hakukohteet, "1.2.246.562.14.2013082908162538927436"), HakemuksenTila.HYVAKSYTTY);
        ass(hakemusMapByHakemusOid, "1.2.246.562.11.00000011992", 2, getHakukohde(hakukohteet, "1.2.246.562.5.02563_04_873_0530"), HakemuksenTila.PERUUNTUNUT);

        // System.out.println PrintHelper.tulostaSijoittelu(sijoitteluAlgorithm));
    }


    private Hakukohde getHakukohde(List<Hakukohde> hakukohteet, String hakukohdeOid) {
        for (Hakukohde hakukohde : hakukohteet) {
            if (hakukohdeOid.equals(hakukohde.getOid())) {
                return hakukohde;
            }
        }
        return null;
    }

    private Hakemus getHakemus(List<Hakemus> hakemukset, String hakemusOid) {
        for (Hakemus hakemus : hakemukset) {
            if (hakemusOid.equals(hakemus.getHakemusOid())) {
                return hakemus;
            }
        }
        return null;
    }

    private void ass(Map<String, List<Hakemus>> oid, String hakemusOid, int hakutoive, Hakukohde hakukohde, HakemuksenTila tila) {

        for (Valintatapajono jono : hakukohde.getValintatapajonot()) {
            Hakemus hakemusHakukohteessa = getHakemus(jono.getHakemukset(), hakemusOid);
            if (hakemusHakukohteessa == null) {
                continue;
            }
            for (Hakemus hakemus : oid.get(hakemusOid)) {
                if (hakutoive == hakemus.getPrioriteetti()) {
                    LOG.debug("Hakemuksen {} tila tarkistus {} == {}",
                            new Object[] { hakemus.getHakemusOid(), hakemus.getTila(), tila });
                    Assertions.assertTrue(tila.equals(hakemus.getTila()));
                    return;
                }
            }
        }
        // LOG.debug("Hakukohteen {} hakemus {} hakutoiveella {} ja tilalla {} ei loytynyt",
        //         new Object[] {hakukohde.getOid(), hakemusOid, hakutoive, tila });
        Assertions.fail("Hakukohteen {"+hakukohde.getOid()+ "} hakemus {"+hakemusOid+ "} hakutoiveella {" +hakutoive+ "} ja tilalla {"+tila+ "} ei loytynyt");
    }

}
