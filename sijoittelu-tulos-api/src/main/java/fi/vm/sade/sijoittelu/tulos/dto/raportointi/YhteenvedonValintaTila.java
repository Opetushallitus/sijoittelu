package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;

public enum YhteenvedonValintaTila {

    HYVAKSYTTY, HARKINNANVARAISESTI_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY, VARALLA, PERUUTETTU, PERUNUT, HYLATTY, PERUUNTUNUT, KESKEN;

    public static YhteenvedonValintaTila fromHakemuksenTila(HakemuksenTila hakemusTila) {
        return YhteenvedonValintaTila.valueOf(hakemusTila.name());
    }
}
