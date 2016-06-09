package fi.vm.sade.sijoittelu.laskenta.service.business;

import java.util.Date;

public class StaleReadException extends RuntimeException {
    public StaleReadException(String hakuOid, String hakukohdeOid, String valintatapajonoOid, String hakemusOid, Date latestChangeFromDb, Date changeRead) {
        super(String.format("Yritettiin muokata muuttunutta valintatulosta. haku: %s, hakukohde: %s, valintatapajono: %s, hakemus: %s, " +
            "viimeinen muutos: %s, muutettu olio haettu: %s, erotus millisekunteina: %s",
                hakuOid, hakukohdeOid, valintatapajonoOid, hakemusOid,
                latestChangeFromDb, changeRead, latestChangeFromDb.getTime() - changeRead.getTime()));
    }
}
