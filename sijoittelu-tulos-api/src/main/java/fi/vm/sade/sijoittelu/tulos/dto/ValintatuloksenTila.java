package fi.vm.sade.sijoittelu.tulos.dto;

public enum ValintatuloksenTila {
    EI_VASTAANOTETTU_MAARA_AIKANA, // Hakija ei ole ilmoittanut paikkaa vastaanotetuksi maaraaikana ja on nain ollen hylatty
    PERUNUT,                       // Hakija ei ota paikkaa vastaan
    PERUUTETTU,                    // Hakijan tila on peruutettu
    EHDOLLISESTI_VASTAANOTTANUT,    // Ehdollisesti vastaanottanut
    VASTAANOTTANUT_SITOVASTI,       // Sitovasti vastaanottanut, kk-tila
    KESKEN;

    public boolean isVastaanottanut() {
        return VASTAANOTTANUT_SITOVASTI.equals(this);
    }
}
