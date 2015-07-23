package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;

import org.junit.Test;
import static junit.framework.Assert.*;

import java.util.List;

public class PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelleTest {
    List<Hakukohde> hakukohdeList = TestHelper.readHakukohteetListFromJson("testdata/sijoittelu_hakukohde.json");
    List<Valintatulos> valintatulosList = TestHelper.readValintatulosListFromJson("testdata/sijoittelu_valintatulos.json");
    final PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelle postProcessor =
            new PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelle();

    @Test
    public void testEhdollisenVastaanotonSiirtyminenYlospain() {
        final SijoitteluajoWrapper sijoitteluAjo = luoSijoitteluAjonTulokset();

        assertHakemuksenTila(sijoitteluAjo, "1.2.246.562.20.87061484133", "14345398388996844110591962067736", "1.2.246.562.11.00003933242", HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
        assertHakemuksenTila(sijoitteluAjo, "1.2.246.562.20.87061484132", "14345398388996844110591962067735", "1.2.246.562.11.00003933242", HakemuksenTila.PERUUNTUNUT);

        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, haeValintatulosHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067736", sijoitteluAjo).getTila());
        assertEquals(ValintatuloksenTila.KESKEN, haeValintatulosHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067735", sijoitteluAjo).getTila());
    }

    private void assertHakemuksenTila(SijoitteluajoWrapper sijoitteluajoWrapper, String hakukohdeOid, String valintatapajonoOid, String hakemusOid, HakemuksenTila hakemuksenTila) {

        assertTrue(sijoitteluajoWrapper.getHakukohteet().stream().filter(hk -> hk.getHakukohde().getOid().equals(hakukohdeOid))
                .flatMap(hk -> hk.getValintatapajonot().stream())
                .filter(j -> j.getValintatapajono().getOid().equals(valintatapajonoOid))
                .flatMap(j -> j.getHakemukset().stream())
                .filter(h -> h.getHakemus().getHakemusOid().equals(hakemusOid))
                .anyMatch(h -> h.getHakemus().getTila().equals(hakemuksenTila)));

    }

    private Valintatulos haeValintatulosHakemukselle(String hakemusOid, String valintatapajonoOid, SijoitteluajoWrapper sijoitteluAjo) {
        return sijoitteluAjo.getMuuttuneetValintatulokset().stream().filter(valintatulos -> valintatulos.getHakemusOid().equals(hakemusOid) && valintatulos.getValintatapajonoOid().equals(valintatapajonoOid)).findFirst().get();
    }

    private SijoitteluajoWrapper luoSijoitteluAjonTulokset() {
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohdeList, valintatulosList);
        SijoittelunTila algorithm = SijoitteluAlgorithm.sijoittele(sijoitteluajoWrapper);
        final SijoitteluajoWrapper sijoitteluAjo = sijoitteluajoWrapper;
        System.out.println("sijoitteluAjo.getMuuttuneetValintatulokset().size(): " + sijoitteluAjo.getMuuttuneetValintatulokset().size());
        sijoitteluAjo.getMuuttuneetValintatulokset().forEach(vt -> {
            System.out.println("Valintatulos: " + vt.getHakemusOid() + " | " + vt.getValintatapajonoOid() + " | " + vt.getTila() + " | " + vt.getLogEntries().size());
        });
        //sijoitteluAjo.getMuuttuneetValintatulokset().addAll(valintatulosList);
        System.out.println(PrintHelper.tulostaSijoittelu(algorithm));
        return sijoitteluAjo;
    }
}
