package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactoryImpl;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.junit.Test;
import static junit.framework.Assert.*;

import java.util.List;

public class PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkausTest {
    List<Hakukohde> hakukohdeList = TestHelper.readHakukohteetListFromJson("testdata/sijoittelu_hakukohde_peruuntunut.json");
    List<Valintatulos> valintatulosList = TestHelper.readValintatulosListFromJson("testdata/sijoittelu_valintatulos_peruuntunut.json");
    final PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus postProcessor =
            new PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus();

    @Test
    public void testPeruuntuneenHakemuksetVastaanotonMuokkaus() {
        final SijoitteluajoWrapper sijoitteluAjo = luoSijoitteluAjonTulokset();
        postProcessor.process(sijoitteluAjo);
        // ylin toive peruuntunut, valintatuloksentila muuttunut kesken varasijalta hyvaksytyksi
        assertEquals(ValintatuloksenTila.KESKEN, haeValintatulosHakemukselle("1.2.246.562.11.00003933243", sijoitteluAjo).getTila());
    }

    private Valintatulos haeValintatulosHakemukselle(String hakemusOid, SijoitteluajoWrapper sijoitteluAjo) {
        return sijoitteluAjo.getMuuttuneetValintatulokset().stream().filter(valintatulos -> valintatulos.getHakemusOid().equals(hakemusOid)).findFirst().get();
    }

    private SijoitteluajoWrapper luoSijoitteluAjonTulokset() {
        SijoitteluAlgorithmFactoryImpl factory = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm algorithm = factory.constructAlgorithm(hakukohdeList, valintatulosList);
        final SijoitteluajoWrapper sijoitteluAjo = algorithm.getSijoitteluAjo();
        sijoitteluAjo.getMuuttuneetValintatulokset().addAll(valintatulosList);
        return sijoitteluAjo;
    }
}
