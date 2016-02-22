package fi.vm.sade.sijoittelu.domain;

public enum ValintatuloksenTila {
    EI_VASTAANOTETTU_MAARA_AIKANA, // Hakija ei ole ilmoittanut paikkaa vastaanotetuksi maaraaikana ja on nain ollen hylatty
    PERUNUT,                       // Hakija ei ota paikkaa vastaan
    PERUUTETTU,                    // Hakijan tila on peruutettu
    EHDOLLISESTI_VASTAANOTTANUT,   // Ehdollisesti vastaanottanut
    VASTAANOTTANUT_SITOVASTI,      // Sitovasti vastaanottanut
    KESKEN
}
