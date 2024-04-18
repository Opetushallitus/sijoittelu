package fi.vm.sade.sijoittelu;

import com.google.gson.GsonBuilder;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by kjsaila on 18/11/14.
 */
public class OhjausparametritTest {

    @Test
    public void testParametrit() throws IOException {
        String json = "{ \"target\": \"1.2.246.562.29.173465377510\", \"__modified__\": 1416309364472, \"__modifiedBy__\": \"1.2.246.562.24.47840234552\", \"PH_TJT\": {\"date\": null}, \"PH_HKLPT\": {\"date\": null}, \"PH_HKMT\": {\"date\": null}, \"PH_KKM\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_HVVPTP\": {\"date\": null}, \"PH_KTT\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_OLVVPKE\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_VLS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_SS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_JKLIP\": {\"date\": null}, \"PH_HKP\": {\"date\": null}, \"PH_VTSSV\": {\"date\": 1416866395389}, \"PH_VSSAV\": {\"date\": 1416866458888}, \"PH_VTJH\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_EVR\": {\"date\": null}, \"PH_OPVP\": {\"date\": null}, \"PH_HPVOA\": {\"date\": null}, \"PH_IP\": {\"date\": null} }";

        LocalDate haluttu = LocalDate.of(2014, 11, 24);
        Long timestamp = Long.valueOf("1416866395389");

        ParametriDTO gson = new GsonBuilder().create().fromJson(json, ParametriDTO.class);
        LocalDate res = LocalDateTime.ofInstant(new Date(gson.getPH_VTSSV().getDate()).toInstant(), ZoneId.systemDefault()).toLocalDate();
        Assertions.assertEquals(haluttu, res);
        Assertions.assertEquals(timestamp, gson.getPH_VTSSV().getDate());


    }
}
