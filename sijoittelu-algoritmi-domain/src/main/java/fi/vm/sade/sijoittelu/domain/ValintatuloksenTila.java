package fi.vm.sade.sijoittelu.domain;

public enum ValintatuloksenTila {
    EI_VASTAANOTETTU_MAARA_AIKANA(false), // Hakija ei ole ilmoittanut paikkaa vastaanotetuksi maaraaikana ja on nain ollen hylatty
    PERUNUT(false),                       // Hakija ei ota paikkaa vastaan
    PERUUTETTU(false),                    // Hakijan tila on peruutettu
    OTTANUT_VASTAAN_TOISEN_PAIKAN(false), // Hakija ottanut vastaan toisen opiskelupaikan yhden paikan säännön piirissä
    EHDOLLISESTI_VASTAANOTTANUT(false),   // Ehdollisesti vastaanottanut
    VASTAANOTTANUT_SITOVASTI(true),       // (Sitovasti) vastaanottanut
    KESKEN(false);

    public final boolean sitova;

    ValintatuloksenTila(boolean sitova) {
        this.sitova = sitova;
    }
}
