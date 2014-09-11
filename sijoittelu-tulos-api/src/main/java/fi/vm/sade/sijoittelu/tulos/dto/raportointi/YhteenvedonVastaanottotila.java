package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

public enum YhteenvedonVastaanottotila {
    KESKEN,
    VASTAANOTTANUT,
    EI_VASTAANOTETTU_MAARA_AIKANA, // Hakija ei ole ilmoittanut paikkaa vastaanotetuksi maaraaikana ja on nain ollen hylatty
    PERUNUT,                       // Hakija ei ota paikkaa vastaan
    PERUUTETTU,                    // Hakijan tila on peruutettu
    EHDOLLISESTI_VASTAANOTTANUT,    // Ehdollisesti vastaanottanut
}
