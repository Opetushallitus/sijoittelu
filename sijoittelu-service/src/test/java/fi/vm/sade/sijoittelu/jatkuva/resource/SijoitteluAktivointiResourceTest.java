package fi.vm.sade.sijoittelu.jatkuva.resource;

import fi.vm.sade.sijoittelu.jatkuva.dao.JatkuvaSijoitteluDAO;
import fi.vm.sade.sijoittelu.jatkuva.dto.JatkuvaSijoittelu;
import fi.vm.sade.testing.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Date;

@ActiveProfiles("test")
public class SijoitteluAktivointiResourceTest extends AbstractIntegrationTest {

  @Autowired
  private JatkuvaSijoitteluDAO jatkuvaSijoitteluDAO;

  @Test
	public void testSijoitteluDAO() {
    String hakuOID = "hakuOID";
    Instant aloitus = Instant.now().with(ChronoField.MICRO_OF_SECOND, 0);
    int ajotiheys = 5;

    // tallennetaan haun tiedot
    this.jatkuvaSijoitteluDAO.paivitaSijoittelunAloitusajankohta(
        hakuOID,
        aloitus.toEpochMilli(),
        ajotiheys);

    // oid:lla hakeminen palauttaa samat tiedot
    JatkuvaSijoittelu jatkuvaSijoittelu = this.jatkuvaSijoitteluDAO.hae(hakuOID);
    Assertions.assertEquals(hakuOID, jatkuvaSijoittelu.getHakuOid());
    Assertions.assertEquals(aloitus, jatkuvaSijoittelu.getAloitusajankohta().toInstant());
    Assertions.assertEquals(ajotiheys, jatkuvaSijoittelu.getAjotiheys());
    Assertions.assertEquals(false, jatkuvaSijoittelu.isAjossa());
    Assertions.assertEquals(null, jatkuvaSijoittelu.getViimeksiAjettu());
    Assertions.assertEquals(null, jatkuvaSijoittelu.getVirhe());

    // kaikki haut sisältävät vain tallennetun haun
    Assertions.assertEquals(1, this.jatkuvaSijoitteluDAO.hae().size());
    Assertions.assertEquals(hakuOID, this.jatkuvaSijoitteluDAO.hae().iterator().next().getHakuOid());

    // ajossatila päivittyy oikein
    this.jatkuvaSijoitteluDAO.merkkaaSijoittelunAjossaTila(hakuOID, true);
    Assertions.assertEquals(true, this.jatkuvaSijoitteluDAO.hae(hakuOID).isAjossa());

    // merkkaa ajetuksi timestamp päivittyy oikein
    this.jatkuvaSijoitteluDAO.merkkaaSijoittelunAjetuksi(hakuOID);
    Assertions.assertTrue(this.jatkuvaSijoitteluDAO.hae(hakuOID).getViimeksiAjettu()
        .before(new Date()));
    Assertions.assertTrue(this.jatkuvaSijoitteluDAO.hae(hakuOID).getViimeksiAjettu()
        .after(Date.from(Instant.now().minusSeconds(1))));

    // sijoittelun poisto toimii
    this.jatkuvaSijoitteluDAO.poistaSijoittelu(hakuOID);
    Assertions.assertEquals(null, this.jatkuvaSijoitteluDAO.hae(hakuOID));
  }
}
