package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

public class PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkausTest {
    final PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus postProcessor =
            new PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus();

    @Test
    public void testPeruuntuneenHakemuksetVastaanotonMuokkaus() {
        List<Hakukohde> hakukohdeList = TestHelper.readHakukohteetListFromJson("testdata/sijoittelu_hakukohde_peruuntunut.json");
        List<Valintatulos> valintatulosList = TestHelper.readValintatulosListFromJson("testdata/sijoittelu_valintatulos_peruuntunut.json");
        final SijoitteluajoWrapper sijoitteluAjo = luoSijoitteluAjonTulokset(hakukohdeList, valintatulosList);
        // ylempi hakukohde on nyt hyvaksytty varasijalta
        assertEquals(HakemuksenTila.VARASIJALTA_HYVAKSYTTY, haeJononTilaHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067734", sijoitteluAjo).getHakemus().getTila());
        // vastaanottotieto siirtyy ylemmälle hakukohteelle
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, haeValintatulosHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067734", sijoitteluAjo).getTila());
        // alempi hakukohde on nyt peruuntunut molemmissa sen jonoissa
        assertEquals(HakemuksenTila.PERUUNTUNUT, haeJononTilaHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067735", sijoitteluAjo).getHakemus().getTila());
        assertEquals(HakemuksenTila.PERUUNTUNUT, haeJononTilaHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067736", sijoitteluAjo).getHakemus().getTila());
        // vastaanottotieto poistuu alemmalta
        //assertEquals(ValintatuloksenTila.KESKEN, haeValintatulosHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067735", sijoitteluAjo).getTila());
        assertEquals(ValintatuloksenTila.KESKEN, haeValintatulosHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067736", sijoitteluAjo).getTila());
    }

    @Test
    public void testJononPeruuntuminenEiPoistaVastaanottoa() {
        List<Hakukohde> hakukohdeList = TestHelper.readHakukohteetListFromJson("testdata/sijoittelu_hakukohteen_toinen_jono_peruuntunut.json");
        List<Valintatulos> valintatulosList = TestHelper.readValintatulosListFromJson("testdata/sijoittelu_valintatulos_peruuntunut.json");
        final SijoitteluajoWrapper sijoitteluAjo = luoSijoitteluAjonTulokset(hakukohdeList, valintatulosList);
        // ylempi jono on nyt hyvaksytty
        assertEquals(HakemuksenTila.HYVAKSYTTY, haeJononTilaHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067735", sijoitteluAjo).getHakemus().getTila());
        // vastaanottotieto näkyy ylemmälle
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, haeValintatulosHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067735", sijoitteluAjo).getTila());
        // alempi jono nyt peruuntunut
        assertEquals(HakemuksenTila.PERUUNTUNUT, haeJononTilaHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067736", sijoitteluAjo).getHakemus().getTila());
        // vastaanottotieto ei häviä
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, haeValintatulosHakemukselle("1.2.246.562.11.00003933242", "14345398388996844110591962067736", sijoitteluAjo).getTila());
    }

    private HakemusWrapper haeJononTilaHakemukselle(String hakemusOid, String valintatapajonoOid, SijoitteluajoWrapper sijoitteluAjo) {
        return sijoitteluAjo.getHakukohteet().stream()
                .flatMap(hakukohde -> hakukohde.getValintatapajonot().stream())
                .filter(jono -> jono.getValintatapajono().getOid().equals(valintatapajonoOid))
                .flatMap(jono -> jono.getHakemukset().stream())
                .filter(hakemus -> hakemus.getHakemus().getHakemusOid().equals(hakemusOid))
                .findFirst()
                .get();
    }
    private Valintatulos haeValintatulosHakemukselle(String hakemusOid, String valintatapajonoOid, SijoitteluajoWrapper sijoitteluAjo) {
        return sijoitteluAjo.getMuuttuneetValintatulokset().stream().filter(valintatulos -> valintatulos.getHakemusOid().equals(hakemusOid) && valintatulos.getValintatapajonoOid().equals(valintatapajonoOid)).findFirst().get();
    }

    private SijoitteluajoWrapper luoSijoitteluAjonTulokset(List<Hakukohde> hakukohdeList, List<Valintatulos> valintatulosList) {
        hakukohdeList.forEach(h -> h.getValintatapajonot().forEach(v -> v.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true)));
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohdeList, valintatulosList, Collections.emptyMap());
        sijoitteluajoWrapper.setKKHaku(true);
        SijoittelunTila tila = SijoitteluAlgorithmUtil.sijoittele(sijoitteluajoWrapper);
        System.out.println(PrintHelper.tulostaSijoittelu(tila));
        final SijoitteluajoWrapper sijoitteluAjo = sijoitteluajoWrapper;
        return sijoitteluAjo;
    }
}
