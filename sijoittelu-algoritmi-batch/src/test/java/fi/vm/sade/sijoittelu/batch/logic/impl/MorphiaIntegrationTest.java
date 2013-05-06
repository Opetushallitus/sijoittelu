
/**package fi.vm.sade.sijoittelu.batch.logic.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.sijoittelu.batch.dao.Dao;
import fi.vm.sade.sijoittelu.batch.logic.impl.itutil.FlapdoodleMongoDbTest;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;

 * 
 * @author Kari Kammonen

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-sijoittelu-batch-mongo.xml" })
@ActiveProfiles()
public class MorphiaIntegrationTest extends FlapdoodleMongoDbTest {

    @Autowired
    private Dao dao;

    @Test
    public void testPersist() {

        SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
        sijoitteluAjo.setSijoitteluajoId(123L);
        sijoitteluAjo.setEndMils(123L);
        sijoitteluAjo.setStartMils(32323L);

        HakukohdeItem hki = new HakukohdeItem();
        hki.setHakukohde(new Hakukohde());
        sijoitteluAjo.getHakukohteet().add(hki);
        sijoitteluAjo.getHakukohteet().get(0).getHakukohde().getValintatapajonot().add(new Valintatapajono());
        sijoitteluAjo.getHakukohteet().get(0).getHakukohde().getValintatapajonot().get(0).getHakemukset()
                .add(new Hakemus());
        sijoitteluAjo.getHakukohteet().get(0).getHakukohde().getValintatapajonot().get(0).getHakemukset()
                .add(new Hakemus());

        dao.persistSijoitteluAjo(sijoitteluAjo);
        SijoitteluAjo b = dao.loadSijoitteluajo(123L);

        Assert.assertNotNull(b);
        Assert.assertEquals(new Long(123), b.getSijoitteluajoId());
    }

}
 *
 */