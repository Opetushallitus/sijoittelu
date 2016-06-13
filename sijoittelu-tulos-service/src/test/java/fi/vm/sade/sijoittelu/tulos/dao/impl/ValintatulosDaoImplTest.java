package fi.vm.sade.sijoittelu.tulos.dao.impl;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.*;
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

import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class ValintatulosDaoImplTest {
    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    String hakuOid = "1.2.246.562.5.2013080813081926341928";

    Date aikaNyt = new Date();

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
            setHakuOid(hakuOid, "");
            setHakukohdeOid("1.2.246.562.5.72607738902", "");
            setHakemusOid("1.2.246.562.11.00000441369", "");
            setTila(ValintatuloksenTila.KESKEN, "");
            setIlmoittautumisTila(IlmoittautumisTila.LASNA, "");
            setValintatapajonoOid("14090336922663576781797489829886", "");
            setJulkaistavissa(true, "");
            setHyvaksymiskirjeLahetetty(aikaNyt, "", JARJESTELMA);
            setEhdollisestiHyvaksyttavissa(true, "", JARJESTELMA);
        }};

        final List<Valintatulos> mergattu = valintatulosDao.mergaaValintatulos(Arrays.asList(kohde), Arrays.asList(tulos));
        assertNotNull(mergattu);
        assertEquals(ValintatuloksenTila.KESKEN, mergattu.get(0).getTila());
        assertEquals(IlmoittautumisTila.EI_TEHTY, mergattu.get(0).getIlmoittautumisTila());
        assertEquals(true, mergattu.get(0).getJulkaistavissa());
        assertEquals(true, mergattu.get(0).getEhdollisestiHyvaksyttavissa());
        assertEquals(aikaNyt, mergattu.get(0).getHyvaksymiskirjeLahetetty());
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
            setHakuOid(hakuOid, "");
            setHakukohdeOid("1.2.246.562.5.72607738902", "");
            setHakemusOid("1.2.246.562.11.00000441369", "");
            setTila(ValintatuloksenTila.KESKEN, "");
            setIlmoittautumisTila(IlmoittautumisTila.LASNA, "");
            setValintatapajonoOid("14090336922663576781797489829886", "");
            setJulkaistavissa(true, "");
            setHyvaksymiskirjeLahetetty(aikaNyt, "", JARJESTELMA);
        }};

        final List<Valintatulos> mergattu = valintatulosDao.mergaaValintatulos(Arrays.asList(kohde), Arrays.asList(tulos));
        assertNotNull(mergattu);
        assertEquals(ValintatuloksenTila.KESKEN, mergattu.get(0).getTila());
        assertEquals(IlmoittautumisTila.LASNA, mergattu.get(0).getIlmoittautumisTila());
        assertEquals(true, mergattu.get(0).getJulkaistavissa());
        assertEquals(aikaNyt, mergattu.get(0).getHyvaksymiskirjeLahetetty());
    }

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void updateValintatulosWithAcceptanceLetterSent() {
        final String HAKUKOHDE_ID = "1.2.246.562.5.72607738902";
        final String HAKEMUS_OID = "1.2.246.562.11.00000441369";
        final String VALINTATAPAJONO_OID = "14090336922663576781797489829886";

        Valintatulos vt1; // Tämä haetaan kannasta ja päivitetään
        Valintatulos vt2; // Tämä haetaan kannasta päivityksen jälkeen

        vt1 = valintatulosDao.loadValintatulos(HAKUKOHDE_ID, VALINTATAPAJONO_OID, HAKEMUS_OID);
        assertNull(vt1.getHyvaksymiskirjeLahetetty());

        vt1.setHyvaksymiskirjeLahetetty(aikaNyt);
        assertEquals(aikaNyt, vt1.getHyvaksymiskirjeLahetetty());

        valintatulosDao.createOrUpdateValintatulos(vt1);

        vt2 = valintatulosDao.loadValintatulos(HAKUKOHDE_ID, VALINTATAPAJONO_OID, HAKEMUS_OID);

        assertNotNull("Valintatuloksen ei pitäisi olla null", vt2);
        assertNotNull("hyvaksymiskirjeLahetetty-kentän ei pitäisi olla null", vt2.getHyvaksymiskirjeLahetetty());
        assertEquals("hyvaksymiskirjeLahetetty-kentällä pitäisi olla oikea arvo", aikaNyt, vt2.getHyvaksymiskirjeLahetetty());
    }
}
