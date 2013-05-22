package fi.vm.sade.sijoittelu.domain;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 20.5.2013
 * Time: 13:32
 * To change this template use File | Settings | File Templates.
 */
public enum ValintatuloksenTila {
    ILMOITETTU,                    // Hakijalle on ilmoitettu, sijoittelun tulos ei voi muuttaa paikkaa peruuntuneeksi
    VASTAANOTTANUT_LASNA,          // Hakija ottanut paikan vastaan ja on lasna
    VASTAANOTTANUT_POISSAOLEVA,    // Hakija ottanut paikan vastaan ja ilmoittautunut poissaolevaksi
    PERUNUT;                       // Hakija ei ota paikkaa vastaan
}
