package fi.vm.sade.sijoittelu.jatkuva.dao;

import fi.vm.sade.sijoittelu.jatkuva.dto.JatkuvaSijoittelu;
import fi.vm.sade.testing.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
public class JatkuvaSijoitteluDAOTest extends AbstractIntegrationTest {

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
    assertEquals(hakuOID, jatkuvaSijoittelu.getHakuOid());
    assertEquals(aloitus, jatkuvaSijoittelu.getAloitusajankohta().toInstant());
    assertEquals(ajotiheys, jatkuvaSijoittelu.getAjotiheys());
    assertFalse(jatkuvaSijoittelu.isAjossa());
    assertNull(jatkuvaSijoittelu.getViimeksiAjettu());
    assertNull(jatkuvaSijoittelu.getVirhe());

    // kaikki haut sisältävät vain tallennetun haun
    assertEquals(1, this.jatkuvaSijoitteluDAO.hae().size());
    assertEquals(hakuOID, this.jatkuvaSijoitteluDAO.hae().iterator().next().getHakuOid());

    // ajossatila päivittyy oikein
    this.jatkuvaSijoitteluDAO.merkkaaSijoittelunAjossaTila(hakuOID, true);
    assertTrue(this.jatkuvaSijoitteluDAO.hae(hakuOID).isAjossa());

    // merkkaa ajetuksi timestamp päivittyy oikein
    this.jatkuvaSijoitteluDAO.merkkaaSijoittelunAjetuksi(hakuOID);
    assertTrue(this.jatkuvaSijoitteluDAO.hae(hakuOID).getViimeksiAjettu()
        .before(new Date()));
    assertTrue(this.jatkuvaSijoitteluDAO.hae(hakuOID).getViimeksiAjettu()
        .after(Date.from(Instant.now().minusSeconds(1))));

    // sijoittelun poisto toimii
    this.jatkuvaSijoitteluDAO.poistaSijoittelu(hakuOID);
    assertNull(this.jatkuvaSijoitteluDAO.hae(hakuOID));
  }

  @Test
  public void luoJatkuvanSijoittelunAktiivisena() {
    String hakuOID = "hakuOID";
    Instant aloitus = Instant.now().with(ChronoField.MICRO_OF_SECOND, 0);
    int ajotiheys = 5;
    this.jatkuvaSijoitteluDAO.luoJatkuvaSijoittelu(
            hakuOID,
            aloitus.toEpochMilli(),
            ajotiheys);
    JatkuvaSijoittelu jatkuvaSijoittelu = this.jatkuvaSijoitteluDAO.hae(hakuOID);
    assertEquals(hakuOID, jatkuvaSijoittelu.getHakuOid());
    assertEquals(aloitus, jatkuvaSijoittelu.getAloitusajankohta().toInstant());
    assertEquals(ajotiheys, jatkuvaSijoittelu.getAjotiheys());
    assertTrue(jatkuvaSijoittelu.isAjossa());
    assertNull(jatkuvaSijoittelu.getViimeksiAjettu());
    assertNull(jatkuvaSijoittelu.getVirhe());
  }
}
