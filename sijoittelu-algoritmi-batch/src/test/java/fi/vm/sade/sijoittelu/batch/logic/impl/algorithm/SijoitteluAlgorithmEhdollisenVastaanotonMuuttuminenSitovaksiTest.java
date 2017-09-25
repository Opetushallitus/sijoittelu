package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SijoitteluAlgorithmEhdollisenVastaanotonMuuttuminenSitovaksiTest {
    List<Hakukohde> hakukohdeList = TestHelper.readHakukohteetListFromJson("testdata/sijoittelu_hakukohde_ylemman_hakukohteen_vastaanotto_paattynyt.json");
    List<Valintatulos> valintatulosList = TestHelper.readValintatulosListFromJson("testdata/sijoittelu_valintatulos_alempi_ehdollisesti.json");

    @Test
    public void testEhdollisenVastaanotonMuuttuminenSitovaksiKunYlempiPeruuntuu() {
        final SijoitteluajoWrapper sijoitteluAjo = luoSijoitteluAjonTulokset();

        assertHakemuksenTila(sijoitteluAjo, "1.2.246.562.20.87061484132", "14345398388996844110591962067735", "1.2.246.562.11.00003933242", HakemuksenTila.PERUUNTUNUT);

        assertHakemuksenTila(sijoitteluAjo, "1.2.246.562.20.87061484133", "14345398388996844110591962067736", "1.2.246.562.11.00003933242", HakemuksenTila.HYVAKSYTTY);
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, haeValintatulosHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067736", sijoitteluAjo).getTila());
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
        List<Valintatulos> muuttuneetValintatulokset = sijoitteluAjo.getMuuttuneetValintatulokset();
        return muuttuneetValintatulokset.stream().filter(valintatulos -> valintatulos.getHakemusOid().equals(hakemusOid) && valintatulos.getValintatapajonoOid().equals(valintatapajonoOid)).findFirst().get();
    }

    private SijoitteluajoWrapper luoSijoitteluAjonTulokset() {
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohdeList, valintatulosList, Collections.emptyMap());
        sijoitteluajoWrapper.setKKHaku(true);
        sijoitteluajoWrapper.valintatapajonotStream().forEach(v -> v.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true));
        SijoittelunTila algorithm = SijoitteluAlgorithmUtil.sijoittele(sijoitteluajoWrapper);
        final SijoitteluajoWrapper sijoitteluAjo = sijoitteluajoWrapper;
        System.out.println("sijoitteluAjo.getMuuttuneetValintatulokset().size(): " + sijoitteluAjo.getMuuttuneetValintatulokset().size());
        sijoitteluAjo.getMuuttuneetValintatulokset().forEach(vt -> {
            System.out.println("Valintatulos: " + vt.getHakemusOid() + " | " + vt.getValintatapajonoOid() + " | " + vt.getTila());
        });
        //sijoitteluAjo.getMuuttuneetValintatulokset().addAll(valintatulosList);
        System.out.println(PrintHelper.tulostaSijoittelu(algorithm));
        return sijoitteluAjo;
    }
}
