package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;


import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SijoitteluAlgorithmFactoryImplTest {
    @Test
    public void testconstructAlgorithm() {
        SijoitteluAlgorithmFactory sijoitteluAlgorithmFactory = new SijoitteluAlgorithmFactoryImpl();

        SijoitteluAlgorithm sijoitteluAlgorithm = sijoitteluAlgorithmFactory.constructAlgorithm(
                generateHakukohteet(), generateValintatulokset());
    }

    private List<Valintatulos> generateValintatulokset() {
        List<Valintatulos> valintatulokset = new ArrayList<>();

        return valintatulokset;
    }

    private List<Hakukohde> generateHakukohteet() {
        List<Hakukohde> hakukohdes = new ArrayList<>();

        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setValintatapajonot(generateValintatapajono());


        return hakukohdes;
    }

    private List<Valintatapajono> generateValintatapajono() {
        List<Valintatapajono> valintatapajonot = new ArrayList<>();

        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setOid("123");

        ArrayList<Hakemus> hakemukset = (ArrayList<Hakemus>) generateHakemukset();
        valintatapajono.setHakemukset(hakemukset);

        valintatapajonot.add(valintatapajono);
        return valintatapajonot;
    }

    private List<Hakemus> generateHakemukset() {
        List<Hakemus> hakemukset = new ArrayList<>();

        return hakemukset;
    }
}
