package fi.vm.sade.sijoittelu;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactory;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.laskenta.dao.Dao;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

/**
 * User: wuoti Date: 11.11.2013 Time: 15.13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-sijoittelu-batch-mongo.xml" })
public class SijoitteluTest {

    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluTest.class);

    @Autowired
    private Dao dao;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SijoitteluAlgorithmFactory algorithmFactory;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    @Test
    @UsingDataSet(locations = "sijoittelutestidata.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void syksy2013Test() {
        List<Hakukohde> hakukohteet = dao.getHakukohdeForSijoitteluajo(1383916417363L);
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
                        h.setTila(null);
                    }
                }
            }
        }

        SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(hakukohteet, null);
        sijoitteluAlgorithm.start();

        ass(hakemusMapByHakemusOid, "1.2.246.562.11.00000011992", 1, getHakukohde(hakukohteet, "1.2.246.562.14.2013082908162538927436"), HakemuksenTila.HYVAKSYTTY);
        ass(hakemusMapByHakemusOid, "1.2.246.562.11.00000011992", 2, getHakukohde(hakukohteet, "1.2.246.562.5.02563_04_873_0530"), HakemuksenTila.PERUUNTUNUT);

        // System.out.println PrintHelper.tulostaSijoittelu(sijoitteluAlgorithm));
    }

    @Test
    @UsingDataSet(locations = "sijoittelutestidata.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void syksy2013HarkinnanvaraisestiHyvaksytytTest() {
        List<Hakukohde> hakukohteet = dao.getHakukohdeForSijoitteluajo(1383916417363L);
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

        SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(hakukohteet, null);
        sijoitteluAlgorithm.start();

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
                    Assert.assertTrue(tila.equals(hakemus.getTila()));
                    return;
                }
            }
        }
        // LOG.debug("Hakukohteen {} hakemus {} hakutoiveella {} ja tilalla {} ei loytynyt",
        //         new Object[] {hakukohde.getOid(), hakemusOid, hakutoive, tila });
        Assert.fail("Hakukohteen {"+hakukohde.getOid()+ "} hakemus {"+hakemusOid+ "} hakutoiveella {" +hakutoive+ "} ja tilalla {"+tila+ "} ei loytynyt");
    }

}
