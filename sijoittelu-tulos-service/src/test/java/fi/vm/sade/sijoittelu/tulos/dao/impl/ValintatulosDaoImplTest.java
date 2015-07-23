package fi.vm.sade.sijoittelu.tulos.dao.impl;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class ValintatulosDaoImplTest {
    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    String hakuOid = "1.2.246.562.5.2013080813081926341928";

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ValintatulosDao valintatulosDao;

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void mergeValintatulosWithMongoValintatulos() {
        Hakukohde kohde = new Hakukohde() {{
            setOid("1.2.246.562.5.72607738902");
        }};
        Valintatulos tulos = new Valintatulos() {{
            setHakuOid(hakuOid);
            setHakukohdeOid("1.2.246.562.5.72607738902");
            setHakemusOid("1.2.246.562.11.00000441369");
            setTila(ValintatuloksenTila.KESKEN);
            setIlmoittautumisTila(IlmoittautumisTila.LASNA);
            setValintatapajonoOid("14090336922663576781797489829886");
            setJulkaistavissa(true);
        }};

        final List<Valintatulos> mergattu = valintatulosDao.mergaaValintatulos(Arrays.asList(kohde), Arrays.asList(tulos));
        assertNotNull(mergattu);
        assertEquals(ValintatuloksenTila.ILMOITETTU, mergattu.get(0).getTila());
        assertEquals(IlmoittautumisTila.EI_TEHTY, mergattu.get(0).getIlmoittautumisTila());
        assertEquals(false, mergattu.get(0).getJulkaistavissa());
    }

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void mergeValintatulosWithMongoValintatulosWhenHakemusIsPeruuntunut() {
        Hakemus peruuntunut = new Hakemus() {{
            setHakemusOid("1.2.246.562.11.00000441369");
            setTila(HakemuksenTila.PERUUNTUNUT);
        }};
        ArrayList<Hakemus> list = new ArrayList<Hakemus>() {{
            add(peruuntunut);
        }};
        Valintatapajono jono = new Valintatapajono() {{
            setHakemukset(list);
        }};
        Hakukohde kohde = new Hakukohde() {{
            setOid("1.2.246.562.5.72607738902");
            setValintatapajonot(Arrays.asList(jono));
        }};
        Valintatulos tulos = new Valintatulos() {{
            setHakuOid(hakuOid);
            setHakukohdeOid("1.2.246.562.5.72607738902");
            setHakemusOid("1.2.246.562.11.00000441369");
            setTila(ValintatuloksenTila.KESKEN);
            setIlmoittautumisTila(IlmoittautumisTila.LASNA);
            setValintatapajonoOid("14090336922663576781797489829886");
            setJulkaistavissa(true);
        }};

        final List<Valintatulos> mergattu = valintatulosDao.mergaaValintatulos(Arrays.asList(kohde), Arrays.asList(tulos));
        assertNotNull(mergattu);
        assertEquals(ValintatuloksenTila.KESKEN, mergattu.get(0).getTila());
        assertEquals(IlmoittautumisTila.LASNA, mergattu.get(0).getIlmoittautumisTila());
        assertEquals(true, mergattu.get(0).getJulkaistavissa());
    }
}
