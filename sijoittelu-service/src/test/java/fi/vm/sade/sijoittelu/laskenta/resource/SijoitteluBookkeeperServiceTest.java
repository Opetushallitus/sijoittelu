package fi.vm.sade.sijoittelu.laskenta.resource;

import fi.vm.sade.sijoittelu.domain.SijoitteluajonTila;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SijoitteluBookkeeperServiceTest {

    @Test
    public void testUusiSijoitteluajoLuodaanTilaanKesken() throws Exception {
        SijoitteluBookkeeperService sijoitteluBookkeeperService = new SijoitteluBookkeeperService();
        String haku1 = "1.2.345.784";
        Long sijoitteluId1 = 444666888L;

        sijoitteluBookkeeperService.luoUusiSijoitteluAjo(haku1, sijoitteluId1);
        assertTrue(sijoitteluBookkeeperService.getHaunSijoitteluAjo(haku1).getTila().equals(SijoitteluajonTila.KESKEN));
    }

    @Test
    public void testKeskenOlevaaSijoitteluajoaEiVoiKorvataUudella() throws Exception {
        SijoitteluBookkeeperService sijoitteluBookkeeperService = new SijoitteluBookkeeperService();
        String haku1 = "1.2.345.99999999";
        Long sijoitteluId1 = 111222333L;
        Long sijoitteluId2 = 444555666L;

        sijoitteluBookkeeperService.luoUusiSijoitteluAjo(haku1, sijoitteluId1);
        sijoitteluBookkeeperService.luoUusiSijoitteluAjo(haku1, sijoitteluId2);
        assertTrue(sijoitteluBookkeeperService.getHaunSijoitteluAjo(haku1).getSijoitteluAjoId().equals(sijoitteluId1));

        //Nyt pitäisi onnistua, koska ollaan tilassa valmis
        sijoitteluBookkeeperService.merkitseSijoitteluAjonTila(haku1, sijoitteluId1, SijoitteluajonTila.VALMIS);
        sijoitteluBookkeeperService.luoUusiSijoitteluAjo(haku1, sijoitteluId2);
        assertTrue(sijoitteluBookkeeperService.getHaunSijoitteluAjo(haku1).getSijoitteluAjoId().equals(sijoitteluId2));
    }

    @Test
    public void testTilojaValmisTaiVirheEiVoiEnaaMuokata() throws Exception {
        SijoitteluBookkeeperService sijoitteluBookkeeperService = new SijoitteluBookkeeperService();
        String haku1 = "1.2.345.7";
        String haku2 = "1.2.345.888";
        Long sijoitteluId1 = 123456789L;
        Long sijoitteluId2 = 987654321L;

        sijoitteluBookkeeperService.luoUusiSijoitteluAjo(haku1, sijoitteluId1);
        sijoitteluBookkeeperService.merkitseSijoitteluAjonTila(haku1, sijoitteluId1, SijoitteluajonTila.VALMIS);
        sijoitteluBookkeeperService.merkitseSijoitteluAjonTila(haku1, sijoitteluId1, SijoitteluajonTila.KESKEN);
        assertTrue(sijoitteluBookkeeperService.getHaunSijoitteluAjo(haku1).getTila().equals(SijoitteluajonTila.VALMIS));

        sijoitteluBookkeeperService.luoUusiSijoitteluAjo(haku2, sijoitteluId2);
        sijoitteluBookkeeperService.merkitseSijoitteluAjonTila(haku2, sijoitteluId2, SijoitteluajonTila.VIRHE);
        sijoitteluBookkeeperService.merkitseSijoitteluAjonTila(haku2, sijoitteluId2, SijoitteluajonTila.VALMIS);
        assertTrue(sijoitteluBookkeeperService.getHaunSijoitteluAjo(haku2).getTila().equals(SijoitteluajonTila.VIRHE));

        //Nyt pitäisi onnistua, koska edellinen sijoitteluajo on valmis
        sijoitteluBookkeeperService.luoUusiSijoitteluAjo(haku1, sijoitteluId2);
        assertTrue(sijoitteluBookkeeperService.getHaunSijoitteluAjo(haku1).getSijoitteluAjoId().equals(sijoitteluId2));
    }

}