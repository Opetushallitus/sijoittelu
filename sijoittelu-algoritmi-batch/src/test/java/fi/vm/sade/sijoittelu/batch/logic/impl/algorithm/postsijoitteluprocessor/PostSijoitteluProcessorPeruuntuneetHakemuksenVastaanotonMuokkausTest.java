package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAjoCreator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

public class PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkausTest {
    List<Hakukohde> hakukohdeList = TestHelper.readHakukohteetListFromJson("testdata/sijoittelu_hakukohde_peruuntunut.json");
    List<Valintatulos> valintatulosList = TestHelper.readValintatulosListFromJson("testdata/sijoittelu_valintatulos_peruuntunut.json");
    final PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus postProcessor =
            new PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus();

    @Test
    public void testPeruuntuneenHakemuksetVastaanotonMuokkaus() {
        final SijoitteluajoWrapper sijoitteluAjo = luoSijoitteluAjonTulokset();
        // ylin toive peruuntunut, valintatuloksentila muuttunut kesken varasijalta hyvaksytyksi
        assertEquals(ValintatuloksenTila.KESKEN, haeValintatulosHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067736", sijoitteluAjo).getTila());
        assertEquals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, haeValintatulosHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067735", sijoitteluAjo).getTila());
    }

    private Valintatulos haeValintatulosHakemukselle(String hakemusOid, String valintatapajonoOid, SijoitteluajoWrapper sijoitteluAjo) {
        return sijoitteluAjo.getMuuttuneetValintatulokset().stream().filter(valintatulos -> valintatulos.getHakemusOid().equals(hakemusOid) && valintatulos.getValintatapajonoOid().equals(valintatapajonoOid)).findFirst().get();
    }

    private SijoitteluajoWrapper luoSijoitteluAjonTulokset() {
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluAjoCreator.createSijoitteluAjo(hakukohdeList, valintatulosList);
        SijoittelunTila tila = SijoitteluAlgorithm.sijoittele(sijoitteluajoWrapper);
        System.out.println(PrintHelper.tulostaSijoittelu(tila));
        final SijoitteluajoWrapper sijoitteluAjo = sijoitteluajoWrapper;
        return sijoitteluAjo;
    }
}
