package fi.vm.sade.sijoittelu.it;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.sijoittelu.util.AbstractSijoitteluMongoDbTest;
import fi.vm.sade.sijoittelu.util.TestDataGenerator;
import fi.vm.sade.tulos.service.GenericFault;
import fi.vm.sade.tulos.service.TulosService;
import fi.vm.sade.tulos.service.types.HaeHakukohteetKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeHautKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeSijoitteluajotKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakuTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakukohdeTilaTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakukohdeTyyppi;
import fi.vm.sade.tulos.service.types.tulos.SijoitteluajoTyyppi;
import fi.vm.sade.tulos.service.types.tulos.ValintatapajonoTyyppi;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
public class TulosServiceIntegrationTest extends AbstractSijoitteluMongoDbTest {

    @Autowired
    private TestDataGenerator testDataGenerator;

    @Autowired
    private TulosService tulosService;

    @Before
    public void before() {
        testDataGenerator.generateTestData();
    }

    @Test
    public void testHaeHaut() {
        List<HakuTyyppi> hakus = tulosService.haeHaut(null);
        assertEquals(1, hakus.size());

        HaeHautKriteeritTyyppi kriteerit = new HaeHautKriteeritTyyppi();
        kriteerit.getHakuOidLista().add(TestDataGenerator.HAKU_OID);
        kriteerit.getHakuOidLista().add("does not exist");
        hakus = tulosService.haeHaut(kriteerit);
        assertEquals(1, hakus.size());

        kriteerit.getHakuOidLista().clear();
        kriteerit.getHakuOidLista().add("does not exist");
        hakus = tulosService.haeHaut(kriteerit);
        assertEquals(0, hakus.size());
    }

    @Test
    public void testHaeSijoitteluajot() {
        List<SijoitteluajoTyyppi> ajos = tulosService.haeSijoitteluajot(null);
        assertEquals(ajos.size(), 2);

        Collections.sort(ajos, new Comparator<SijoitteluajoTyyppi>() {

            @Override
            public int compare(SijoitteluajoTyyppi o1, SijoitteluajoTyyppi o2) {
                if (o1.getSijoitteluId() < o2.getSijoitteluId()) {
                    return -1;
                } else if (o1.getSijoitteluId() > o2.getSijoitteluId()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        assertEquals(TestDataGenerator.SIJOITTELU_AJO_ID_1, ajos.get(0).getSijoitteluId());
        assertEquals(TestDataGenerator.SIJOITTELU_AJO_ID_2, ajos.get(1).getSijoitteluId());

        HaeSijoitteluajotKriteeritTyyppi kriteerit = new HaeSijoitteluajotKriteeritTyyppi();
        kriteerit.getSijoitteluIdLista().add(TestDataGenerator.SIJOITTELU_AJO_ID_1);
        ajos = tulosService.haeSijoitteluajot(kriteerit);
        assertEquals(ajos.size(), 1);
        assertEquals(TestDataGenerator.SIJOITTELU_AJO_ID_1, ajos.get(0).getSijoitteluId());

        kriteerit.getSijoitteluIdLista().clear();

        // -1 id does not exist
        kriteerit.getSijoitteluIdLista().add(-1L);
        ajos = tulosService.haeSijoitteluajot(kriteerit);
        assertEquals(0, ajos.size());

    }

    @Test
    public void testHaeHakukohteet() {
        List<HakukohdeTyyppi> hakukohdes = tulosService.haeHakukohteet(TestDataGenerator.SIJOITTELU_AJO_ID_1, null);
        assertEquals(3, hakukohdes.size());

        for (HakukohdeTyyppi h : hakukohdes) {
            assertEquals(HakukohdeTilaTyyppi.SIJOITELTU, h.getTila());
            assertEquals(2, h.getValintatapajonos().size());
            for (ValintatapajonoTyyppi vtj : h.getValintatapajonos()) {
                assertEquals(4, vtj.getHakijaList().size());
            }
        }

        hakukohdes = tulosService.haeHakukohteet(TestDataGenerator.SIJOITTELU_AJO_ID_2, null);
        assertEquals(3, hakukohdes.size());

        for (HakukohdeTyyppi h : hakukohdes) {
            assertEquals(HakukohdeTilaTyyppi.SIJOITTELUSSA, h.getTila());
            assertEquals(2, h.getValintatapajonos().size());
            for (ValintatapajonoTyyppi vtj : h.getValintatapajonos()) {
                assertEquals(4, vtj.getHakijaList().size());
            }
        }

        HaeHakukohteetKriteeritTyyppi kriteerit = new HaeHakukohteetKriteeritTyyppi();
        kriteerit.getHakukohdeOidLista().add(TestDataGenerator.HAKUKOHDE_OID_1);
        hakukohdes = tulosService.haeHakukohteet(TestDataGenerator.SIJOITTELU_AJO_ID_1, kriteerit);
        assertEquals(1, hakukohdes.size());
        assertEquals(TestDataGenerator.HAKUKOHDE_OID_1, hakukohdes.get(0).getOid());
        assertEquals(HakukohdeTilaTyyppi.SIJOITELTU, hakukohdes.get(0).getTila());

        kriteerit.getHakukohdeOidLista().add(TestDataGenerator.HAKUKOHDE_OID_2);
        hakukohdes = tulosService.haeHakukohteet(TestDataGenerator.SIJOITTELU_AJO_ID_1, kriteerit);
        assertEquals(2, hakukohdes.size());
    }

    @Test(expected = GenericFault.class)
    public void testHaeHakukohteetWithNonExistingSijoitteluId() {
        tulosService.haeHakukohteet(-1L, null);
    }

}
