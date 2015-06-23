package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;

import java.util.Arrays;
import java.util.List;

public class TilaTaulukot {
    private static final List<HakemuksenTila> hyvaksytytTilat = Arrays.asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
    private static final List<HakemuksenTila> varaTilat = Arrays.asList(HakemuksenTila.VARALLA, HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
    private static final List<IlmoittautumisTila> poissaoloTilat = Arrays.asList(IlmoittautumisTila.POISSA, IlmoittautumisTila.POISSA_KOKO_LUKUVUOSI, IlmoittautumisTila.POISSA_SYKSY);
    private static final List<HakemuksenTila> hylatytTilat = Arrays.asList(HakemuksenTila.PERUNUT, HakemuksenTila.PERUUTETTU, HakemuksenTila.HYLATTY);
    private static final List<HakemuksenTila> yliajettavatHakemuksetTilat = Arrays.asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARALLA, HakemuksenTila.VARASIJALTA_HYVAKSYTTY);

    public static boolean kuuluuHyvaksyttyihinTiloihin(HakemuksenTila tila) {
        return hyvaksytytTilat.contains(tila);
    }

    public static boolean kuuluuVaraTiloihin(HakemuksenTila tila) {
        return varaTilat.contains(tila);
    }

    public static boolean kuuluuPoissaoloTiloihin(IlmoittautumisTila tila) {
        return poissaoloTilat.contains(tila);
    }

    public static boolean kuuluuHylattyihinTiloihin(HakemuksenTila tila) {
        return hylatytTilat.contains(tila);
    }
    public static boolean kuuluuYliajettaviinHakemuksenTiloihin(HakemuksenTila tila) {
        return yliajettavatHakemuksetTilat.contains(tila);
    }
}
