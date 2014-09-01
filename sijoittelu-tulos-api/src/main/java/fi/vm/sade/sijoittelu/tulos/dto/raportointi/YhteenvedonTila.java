package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;

public enum YhteenvedonTila {

    HYVAKSYTTY, HARKINNANVARAISESTI_HYVAKSYTTY, VARALLA, PERUUTETTU, PERUNUT, HYLATTY, PERUUNTUNUT, KESKEN;

    public static YhteenvedonTila fromHakemuksenTila(HakemuksenTila hakemusTila) {
        return YhteenvedonTila.valueOf(hakemusTila.name());
    }
}
