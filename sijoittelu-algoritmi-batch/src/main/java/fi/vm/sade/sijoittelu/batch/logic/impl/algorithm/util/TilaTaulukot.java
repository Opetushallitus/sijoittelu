package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import com.google.common.collect.Sets;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;

import java.util.Set;

public class TilaTaulukot {
    private static final Set<HakemuksenTila> hyvaksytytTilat = Sets.newHashSet(
            HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY
    );
    private static final Set<HakemuksenTila> varaTilat = Sets.newHashSet(
            HakemuksenTila.VARALLA, HakemuksenTila.VARASIJALTA_HYVAKSYTTY
    );
    private static final Set<IlmoittautumisTila> poissaoloTilat = Sets.newHashSet(
            IlmoittautumisTila.POISSA, IlmoittautumisTila.POISSA_KOKO_LUKUVUOSI, IlmoittautumisTila.POISSA_SYKSY
    );
    private static final Set<HakemuksenTila> hylatytTilat = Sets.newHashSet(
            HakemuksenTila.PERUNUT, HakemuksenTila.PERUUTETTU, HakemuksenTila.HYLATTY
    );
    private static final Set<HakemuksenTila> yliajettavatHakemuksetTilat = Sets.newHashSet(
            HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARALLA, HakemuksenTila.VARASIJALTA_HYVAKSYTTY
    );
    private static final Set<HakemuksenTila> vastaanotonMuokattavissaTilat = Sets.newHashSet(
            HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY, HakemuksenTila.PERUNUT, HakemuksenTila.PERUUTETTU
    );

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

    public static boolean kuuluuVastaanotonMuokattavissaTiloihin(HakemuksenTila tila) {
        return vastaanotonMuokattavissaTilat.contains(tila);
    }
}
