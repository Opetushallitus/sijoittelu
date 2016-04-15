package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 * @author Kari Kammonen
 *
 */
public class VastaanottotietoTest {

    /**
     * Testaa perustapaus
     *
     * @throws java.io.IOException
     */
    @Test
    public void testVastaanottotilaPeruutettu() throws IOException {
        // tee sijoittelu
        HakuDTO t = TestHelper.readHakuDTOFromJson("testdata/sijoittelu_vastaanottotieto_case.json");


        List<Hakukohde> hakukohteet = t.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        ArrayList<Valintatulos> valintatuloses = new ArrayList<Valintatulos>();
        Valintatulos valintatulos = new Valintatulos();

        valintatulos.setHakemusOid("1.2.246.562.24.00000000004", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000007", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setTila(ValintatuloksenTila.PERUUTETTU, "");
        valintatulos.setValintatapajonoOid("tkk_jono_1", "");

        valintatuloses.add(valintatulos);

        valintatulos = new Valintatulos();

        valintatulos.setHakemusOid("1.2.246.562.24.00000000003", "");
        valintatulos.setHakijaOid("oid2", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000007", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setTila(ValintatuloksenTila.PERUNUT, "");
        valintatulos.setValintatapajonoOid("tkk_jono_2", "");

        valintatuloses.add(valintatulos);
        System.out.println(PrintHelper.tulostaSijoittelu(SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), new ArrayList<>())));
        System.out.println(PrintHelper.tulostaSijoittelu(SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), new ArrayList<>())));
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), valintatuloses);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        // assertoi
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000004", HakemuksenTila.PERUUTETTU);
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000003", HakemuksenTila.PERUNUT);
        TestHelper.assertoiAinoastaanValittuMyosVarasijalta(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000001");

        TestHelper.assertoiAinoastaanValittuMyosVarasijalta(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.24.00000000006");

    }


    @Test
    public void testVastaanottotilaVastaanottanutSitovasti() throws IOException {
        // tee sijoittelu
        HakuDTO t = TestHelper.readHakuDTOFromJson("testdata/sijoittelu_vastaanottotieto_case.json");

        List<Hakukohde> hakukohteet = t.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        ArrayList<Valintatulos> valintatuloses = new ArrayList<>();
        Valintatulos valintatulos = new Valintatulos();

        valintatulos.setHakemusOid("1.2.246.562.24.00000000006", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000009", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(true, "");
        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        valintatulos.setValintatapajonoOid("opisto_jono_1", "");

        valintatuloses.add(valintatulos);

        valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("1.2.246.562.24.00000000005", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000009", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(true, "");
        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        valintatulos.setValintatapajonoOid("opisto_jono_1", "");

        valintatuloses.add(valintatulos);
        SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), valintatuloses);
        TestHelper.assertoi(hakukohteet.get(2).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000006", HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, valintatuloses);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        // assertoi
        TestHelper.assertoi(hakukohteet.get(2).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000005", HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000006", HakemuksenTila.VARALLA);
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.24.00000000006", HakemuksenTila.VARALLA);



        valintatuloses = new ArrayList<>();
        valintatulos = new Valintatulos();

        valintatulos.setHakemusOid("1.2.246.562.24.00000000003", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000007", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(true, "");
        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        valintatulos.setValintatapajonoOid("tkk_jono_2", "");

        valintatuloses.add(valintatulos);

//        valintatulos = new Valintatulos();
//        valintatulos.setHakemusOid("1.2.246.562.24.00000000006", "");
//        valintatulos.setHakijaOid("oid", "");
//        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000009", "");
//        valintatulos.setHakuOid(t.getHakuOid(), "");
//        valintatulos.setJulkaistavissa(true, "");
//        valintatulos.setTila(ValintatuloksenTila.KESKEN, "");
//        valintatulos.setValintatapajonoOid("opisto_jono_1", "");
//
//        valintatuloses.add(valintatulos);
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, valintatuloses);

        System.out.println(PrintHelper.tulostaSijoittelu(s));



        // assertoi
        TestHelper.assertoi(hakukohteet.get(2).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000006", HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000006", HakemuksenTila.VARALLA);
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.24.00000000006", HakemuksenTila.VARALLA);
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000003", HakemuksenTila.VARALLA);


        valintatuloses = new ArrayList<>();
        valintatulos = new Valintatulos();

        valintatulos.setHakemusOid("1.2.246.562.24.00000000002", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000008", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(false, "");
        valintatulos.setTila(ValintatuloksenTila.KESKEN, "");
        valintatulos.setValintatapajonoOid("esimerkkikoulu_jono_1", "");

        valintatuloses.add(valintatulos);

        valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("1.2.246.562.24.00000000003", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000008", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(true, "");
        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        valintatulos.setValintatapajonoOid("esimerkkikoulu_jono_1", "");

        valintatuloses.add(valintatulos);
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, valintatuloses);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        // assertoi
        TestHelper.assertoi(hakukohteet.get(1).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000002", HakemuksenTila.VARALLA);
        TestHelper.assertoi(hakukohteet.get(1).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000003", HakemuksenTila.PERUUNTUNUT);
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000003", HakemuksenTila.VARALLA);
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.24.00000000003", HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
    }

    @Test
    public void testVastaanottotilaVastaanottanutEhdollisesti() throws IOException {
        // tee sijoittelu
        HakuDTO t = TestHelper.readHakuDTOFromJson("testdata/sijoittelu_vastaanottotieto_case.json");


        List<Hakukohde> hakukohteet = t.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());


        ArrayList<Valintatulos> valintatuloses = new ArrayList<>();
        Valintatulos valintatulos = new Valintatulos();

        valintatulos.setHakemusOid("1.2.246.562.24.00000000006", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000009", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(false, "");
        valintatulos.setTila(ValintatuloksenTila.KESKEN, "");
        valintatulos.setValintatapajonoOid("opisto_jono_1", "");

        valintatuloses.add(valintatulos);

        valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("1.2.246.562.24.00000000005", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000009", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(true, "");
        valintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");
        valintatulos.setValintatapajonoOid("opisto_jono_1", "");

        valintatuloses.add(valintatulos);
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, valintatuloses);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        // assertoi
        TestHelper.assertoi(hakukohteet.get(2).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000005", HakemuksenTila.HYVAKSYTTY);
        TestHelper.assertoi(hakukohteet.get(2).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000006", HakemuksenTila.HYVAKSYTTY);


        valintatuloses = new ArrayList<>();
        valintatulos = new Valintatulos();

        valintatulos.setHakemusOid("1.2.246.562.24.00000000005", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000009", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(false, "");
        valintatulos.setTila(ValintatuloksenTila.KESKEN, "");
        valintatulos.setValintatapajonoOid("opisto_jono_1", "");

        valintatuloses.add(valintatulos);

        valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("1.2.246.562.24.00000000006", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000009", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(true, "");
        valintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");
        valintatulos.setValintatapajonoOid("opisto_jono_1", "");

        valintatuloses.add(valintatulos);
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, valintatuloses);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        // assertoi
        TestHelper.assertoi(hakukohteet.get(2).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000006", HakemuksenTila.HYVAKSYTTY);
        TestHelper.assertoi(hakukohteet.get(2).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000005", HakemuksenTila.HYVAKSYTTY);

        valintatuloses = new ArrayList<>();
        valintatulos = new Valintatulos();

        valintatulos.setHakemusOid("1.2.246.562.24.00000000002", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000008", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(false, "");
        valintatulos.setTila(ValintatuloksenTila.KESKEN, "");
        valintatulos.setValintatapajonoOid("esimerkkikoulu_jono_1", "");

        valintatuloses.add(valintatulos);

        valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("1.2.246.562.24.00000000001", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000008", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(true, "");
        valintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");
        valintatulos.setValintatapajonoOid("esimerkkikoulu_jono_1", "");

        valintatuloses.add(valintatulos);
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, valintatuloses);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        // assertoi
        TestHelper.assertoi(hakukohteet.get(1).getValintatapajonot()
                .get(0), "1.2.246.562.24.000000000010", HakemuksenTila.HYVAKSYTTY);
        TestHelper.assertoi(hakukohteet.get(1).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000003", HakemuksenTila.PERUUNTUNUT);
        TestHelper.assertoi(hakukohteet.get(1).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000002", HakemuksenTila.VARALLA);
        TestHelper.assertoi(hakukohteet.get(1).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000001", HakemuksenTila.VARALLA);

        valintatuloses = new ArrayList<>();
        valintatulos = new Valintatulos();

        valintatulos.setHakemusOid("1.2.246.562.24.00000000002", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000008", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(false, "");
        valintatulos.setTila(ValintatuloksenTila.KESKEN, "");
        valintatulos.setValintatapajonoOid("esimerkkikoulu_jono_1", "");

        valintatuloses.add(valintatulos);

        valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("1.2.246.562.24.00000000003", "");
        valintatulos.setHakijaOid("oid", "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000008", "");
        valintatulos.setHakuOid(t.getHakuOid(), "");
        valintatulos.setJulkaistavissa(true, "");
        valintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");
        valintatulos.setValintatapajonoOid("esimerkkikoulu_jono_1", "");

        valintatuloses.add(valintatulos);
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, valintatuloses);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        // assertoi
        TestHelper.assertoi(hakukohteet.get(1).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000002", HakemuksenTila.VARALLA);
        TestHelper.assertoi(hakukohteet.get(1).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000003", HakemuksenTila.PERUUNTUNUT);

    }

    private List<Hakukohde> tallennaEdellisetTilat(List<Hakukohde> hakukohteet) {
        hakukohteet.stream().forEach(hk -> {
            hk.getValintatapajonot().stream().forEach(jono -> {
                jono.getHakemukset().stream().forEach(h -> {
                    h.setEdellinenTila(h.getTila());
                });
            });
        });
        return hakukohteet;
    }
}
