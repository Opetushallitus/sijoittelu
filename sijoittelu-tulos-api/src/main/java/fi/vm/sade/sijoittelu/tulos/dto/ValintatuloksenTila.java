package fi.vm.sade.sijoittelu.tulos.dto;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 20.5.2013
 * Time: 13:32
 * To change this template use File | Settings | File Templates.
 */
public enum ValintatuloksenTila {
    ILMOITETTU,                    // Hakijalle on ilmoitettu, sijoittelun tulos ei voi muuttaa paikkaa peruuntuneeksi
    VASTAANOTTANUT,
    VASTAANOTTANUT_LASNA,          // Hakija ottanut paikan vastaan ja on lasna
    VASTAANOTTANUT_POISSAOLEVA,    // Hakija ottanut paikan vastaan ja ilmoittautunut poissaolevaksi
    EI_VASTAANOTETTU_MAARA_AIKANA, // Hakija ei ole ilmoittanut paikkaa vastaanotetuksi maaraaikana ja on nain ollen hylatty
    PERUNUT,                       // Hakija ei ota paikkaa vastaan
    PERUUTETTU,                    // Hakijan tila on peruutettu
    EHDOLLISESTI_VASTAANOTTANUT,    // Ehdollisesti vastaanottanut
    VASTAANOTTANUT_SITOVASTI,       // Sitovasti vastaanottanut, kk-tila
    KESKEN
}
