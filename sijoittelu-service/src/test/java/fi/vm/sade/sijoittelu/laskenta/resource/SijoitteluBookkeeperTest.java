package fi.vm.sade.sijoittelu.laskenta.resource;

import org.junit.Test;

import static org.junit.Assert.*;

public class SijoitteluBookkeeperTest {
    @Test
    public void testSijoitteluBookkeeper() throws Exception {
        SijoitteluBookkeeper keeper = SijoitteluBookkeeper.getInstance();

        String haku1 = "1.2.345.7";
        String haku2 = "1.2.345.888";

        Long sijoitteluId1 = 123456789L;
        Long sijoitteluId2 = 987654321L;
        Long sijoitteluId3 = 78402L;

        //Jos haun sijoitteluajon tila on kesken, sitä ei voi korvata uudella
        keeper.luoUusiSijoitteluAjo(haku1, sijoitteluId1);
        keeper.luoUusiSijoitteluAjo(haku2, sijoitteluId3);
        keeper.luoUusiSijoitteluAjo(haku1, sijoitteluId2);
        assertTrue(keeper.getHaunSijoitteluAjo(haku1).getSijoitteluId().equals(sijoitteluId1));
        assertTrue(keeper.getHaunSijoitteluAjo(haku2).getTila().equals(HaunSijoittelunTila.KESKEN));

        keeper.merkitseSijoitteluAjonTila(haku1, sijoitteluId1, HaunSijoittelunTila.VALMIS);
        keeper.merkitseSijoitteluAjonTila(haku1, sijoitteluId1, HaunSijoittelunTila.EI_LOYTYNYT);
        assertTrue(keeper.getHaunSijoitteluAjo(haku1).getTila().equals(HaunSijoittelunTila.VALMIS));

        //Nyt pitäisi onnistua, koska edellinen sijoitteluajo on valmis
        keeper.luoUusiSijoitteluAjo(haku1, sijoitteluId2);
        assertTrue(keeper.getHaunSijoitteluAjo(haku1).getSijoitteluId().equals(sijoitteluId2));

        /*
        keeper.merkitseSijoitteluAjonTila(sijoitteluId1, HaunSijoittelunTila.VALMIS);
        keeper.merkitseHaunSijoitteluajoViite(haku1, sijoitteluId2);

        assertTrue(keeper.getHaunSijoitteluAjo(haku1).equals(sijoitteluId2));
        assertTrue(keeper.haunSijoitteluAjonTila(haku1).equals(HaunSijoittelunTila.KESKEN));
        */
    }


}