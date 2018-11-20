package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuHyvaksyttyihinTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods.hakemuksenTila;

public class LisapaikatHakijaryhmasijoittelunYlitaytonSeurauksena {
    private static HakemusWrapperComparator comparator = new HakemusWrapperComparator();

    //Apumuuttujat login ja jsonin parsimiseen
    private String hakukohdeOid;
    private String valintatapajonoOid;
    private String valintatapajonoNimi;
    private int tilaa;
    private int seuraaviaTasasijalla;
    private int alinHakijaryhmaJonosija;
    private int relevanteistaRyhmistaHyvaksyttyja;
    private int relevantitHakijaryhmakiintiot;
    private int relevanttienKiintioidenYlitys;
    private int alimmallaSijallaHyvaksyttyja;
    private int alimmallaSijallaHakijaryhmastaHyvaksyttyja;
    private List<String> alimmallaSijallaOlevienTiedot;
    private int hakukohteessaHakijaryhmia;
    private List<Pair<String, Integer>> kiintionYlityksetRyhmittain;
    private List<Pair<String, Integer>> paremmatHakemuksetJaJonosijat;
    //--


    private String toLog = "";

    private boolean kaikkiEhdot;
    private int ehdollisetAloituspaikatTapa1;
    private int ehdollisetAloituspaikatTapa2;

    private Set<String> alimpienHyvaksyttyjenHakijaryhmaOids = new HashSet<>();

    LisapaikatHakijaryhmasijoittelunYlitaytonSeurauksena(ValintatapajonoWrapper valintatapajono, int tilaa, int seuraaviaTasasijalla) {
        this.hakukohdeOid = valintatapajono.getHakukohdeWrapper().getHakukohde().getOid();
        this.valintatapajonoOid = valintatapajono.getValintatapajono().getOid();
        this.valintatapajonoNimi = valintatapajono.getValintatapajono().getNimi();
        this.tilaa = tilaa;
        this.seuraaviaTasasijalla = seuraaviaTasasijalla;
        this.hakukohteessaHakijaryhmia = valintatapajono.getHakukohdeWrapper().getHakijaryhmaWrappers().size();

        List<HakemusWrapper> alimmallaSijallaOlevatHyvaksytyt = alimmallaHyvaksytyllaJonosijallaOlevatHyvaksytyt(valintatapajono);
        List<HakemusWrapper> alimmallaSijallaOlevatHakijaryhmastaHyvaksytyt = alimmallaSijallaOlevatHyvaksytyt
                .stream()
                .filter(HakemusWrapper::isHyvaksyttyHakijaryhmastaTallaKierroksella)
                .collect(Collectors.toList());
        alimmallaSijallaOlevatHakijaryhmastaHyvaksytyt.forEach(h -> alimpienHyvaksyttyjenHakijaryhmaOids.addAll(h.getHakemus().getHyvaksyttyHakijaryhmista()));

        kiintionYlityksetRyhmittain = new ArrayList<>();
        for (HakijaryhmaWrapper ryhma : valintatapajono.getHakukohdeWrapper().getHakijaryhmaWrappers()) {
            String ryhmaOid = ryhma.getHakijaryhma().getOid();
            kiintionYlityksetRyhmittain.add(Pair.of(ryhmaOid, hakukohteenKiintionYlitysHakijaryhmalle(valintatapajono, ryhmaOid)));
        }

        List<HakijaryhmaWrapper> relevantitHakijaryhmat = valintatapajono.getHakukohdeWrapper().getHakijaryhmaWrappers().stream()
                .filter(hrw -> alimpienHyvaksyttyjenHakijaryhmaOids.contains(hrw.getHakijaryhma().getOid()))
                .collect(Collectors.toList());

        alimmallaSijallaHyvaksyttyja = alimmallaSijallaOlevatHyvaksytyt.size();
        alimmallaSijallaHakijaryhmastaHyvaksyttyja = alimmallaSijallaOlevatHakijaryhmastaHyvaksytyt.size();

        relevantitHakijaryhmakiintiot = 0;
        relevanteistaRyhmistaHyvaksyttyja = 0;
        for (HakijaryhmaWrapper hw : relevantitHakijaryhmat) {
            relevantitHakijaryhmakiintiot += hw.getHakijaryhma().getKiintio();
            relevanteistaRyhmistaHyvaksyttyja += hakijaryhmastaHyvaksyttyjaHakukohteenKaikissaJonoissa(valintatapajono.getHakukohdeWrapper(), hw.getHakijaryhma().getOid());
        }
        relevanttienKiintioidenYlitys = relevanteistaRyhmistaHyvaksyttyja - relevantitHakijaryhmakiintiot; //kiintiön ylitys tämän jonon viimeisellä sijalla olevien hakijaryhmissä

        //EHTO: Tämän jonon alimmalla hyväksytyllä jonosijalla on yli yksi hakijaryhmästä hyväksyttyä
        boolean ehto1 = alimmallaSijallaOlevatHakijaryhmastaHyvaksytyt.size() > 1;

        //EHTO: Muista hakukohteen jonoista ei löydy samoista hakijaryhmistä hyväksyttyjä huonommalla jonosijalla
        boolean ehto2 = huonoimmatHakijaryhmistaHyvaksytytOvatTastaJonosta(valintatapajono, alimpienHyvaksyttyjenHakijaryhmaOids);

        //EHTO: Tämän jonon alimman hyväksytyn jonosijan hakijaryhmistä on hyväksytty hakijoita yli kiintiön verran
        boolean ehto3 = relevanttienKiintioidenYlitys > 0;

        this.kaikkiEhdot = ehto1 && ehto2 && ehto3;

        //Tapa 1: Alimmalla hyväksytyllä jonosijalla olevien hakijaryhmistä hyväksyttyjen ja näiden ryhmien kiintiön erotus
        ehdollisetAloituspaikatTapa1 = relevanttienKiintioidenYlitys;
        //Tapa 2: alimmalla hyväksytyllä jonosijalla olevien hakijaryhmästä hyväksyttyjen määrä vähennettynä yhdellä
        ehdollisetAloituspaikatTapa2 = alimmallaSijallaOlevatHakijaryhmastaHyvaksytyt.size() - 1;

        alinHakijaryhmaJonosija = alimmallaSijallaOlevatHakijaryhmastaHyvaksytyt.size() > 0 ? alimmallaSijallaOlevatHakijaryhmastaHyvaksytyt.get(0).getHakemus().getJonosija() : -1;
        List<HakemusWrapper> kiinnostavat = samallaTaiParemmallaJonosijallaOlevatHakijaryhmaanKuulumattomat(valintatapajono, alinHakijaryhmaJonosija);
        paremmatHakemuksetJaJonosijat = kiinnostavat.stream()
                .map(hw -> Pair.of(hw.getHakemus().getHakemusOid(), hw.getHakemus().getJonosija()))
                .collect(Collectors.toList());


        alimmallaSijallaOlevienTiedot = alimmallaSijallaOlevatHyvaksytyt.stream().map(hw -> "(" + hw.getHakemus().getHakemusOid() + ", hyvaksyttyHakijaryhmista: " + hw.getHakemus().getHyvaksyttyHakijaryhmista() + ")").collect(Collectors.toList());
    }


    private static List<HakemusWrapper> samallaTaiParemmallaJonosijallaOlevatHakijaryhmaanKuulumattomat(ValintatapajonoWrapper jono, int jonosija) {
        return jono.getHakemukset().stream()
                .filter(hw -> hw.getHakemus().getJonosija() <= jonosija)
                .filter(hw -> !kuuluuHyvaksyttyihinTiloihin(hw.getHakemus().getTila()))
                .filter(hw -> jono.getHakukohdeWrapper().getHakijaryhmaWrappers().stream()
                        .noneMatch(hrw -> hrw.getHakijaryhma().getHakemusOid().contains(hw.getHakemus().getHakemusOid())))
                .collect(Collectors.toList());
    }

    private static List<HakemusWrapper> alimmallaHyvaksytyllaJonosijallaOlevatHyvaksytyt(ValintatapajonoWrapper wrapper) {
        Optional<HakemusWrapper> hakemusHyvaksytyistaMatalimmallaJonosijalla = wrapper.getHakemukset()
                .stream()
                .filter(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)))
                .min((h1, h2) -> comparator.compare(h2, h1));
        if (hakemusHyvaksytyistaMatalimmallaJonosijalla.isPresent()) {
            HakemusWrapper h = hakemusHyvaksytyistaMatalimmallaJonosijalla.get();
            int matalinJonosija = h.getHakemus().getJonosija();
            return wrapper.getHakemukset().stream()
                    .filter(hw -> hw.getHakemus().getJonosija() == matalinJonosija)
                    .collect(Collectors.toList());
        } else return Collections.emptyList();
    }

    private static int hakijaryhmastaHyvaksyttyjaHakukohteenKaikissaJonoissa(HakukohdeWrapper hw, String hakijaryhmaOid) {
        List<ValintatapajonoWrapper> kaikkiHakukohteenJonot = hw.getValintatapajonot();

        int hyvaksyttyjaYhteensa = 0;
        for (ValintatapajonoWrapper jonoWrapper : kaikkiHakukohteenJonot) {
            hyvaksyttyjaYhteensa += jonoWrapper.getHakemukset()
                    .stream()
                    .filter(h -> h.getHakemus().getHyvaksyttyHakijaryhmista().contains(hakijaryhmaOid))
                    .collect(Collectors.toList()).size();
        }
        return hyvaksyttyjaYhteensa;
    }

    private static boolean huonoimmatHakijaryhmistaHyvaksytytOvatTastaJonosta(ValintatapajonoWrapper tamaJono, Set<String> hakijaryhmaOids) {
        int huonoinJonosija = -1;
        int huonoinJonoPrioriteetti = -1; //tiebreaker jos sama jonosija
        for (ValintatapajonoWrapper ehdokasJono : tamaJono.getHakukohdeWrapper().getValintatapajonot()) {
            int huonoinJonosijaEhdokasjonosta = huonoimpienHakijaryhmistaHyvaksyttyjenJonosija(ehdokasJono, hakijaryhmaOids);
            if (huonoinJonosijaEhdokasjonosta >= huonoinJonosija) {
                if (huonoinJonosijaEhdokasjonosta == huonoinJonosija) {
                    if (huonoinJonoPrioriteetti < ehdokasJono.getValintatapajono().getPrioriteetti()) {
                        huonoinJonoPrioriteetti = ehdokasJono.getValintatapajono().getPrioriteetti(); //Löytyi sama jonosija heikomman prioriteetin jonosta, päivitetään siihen
                    }
                } else {
                    huonoinJonosija = huonoinJonosijaEhdokasjonosta; //Löytyi heikompi jonosija, päivitetään siihen
                    huonoinJonoPrioriteetti = ehdokasJono.getValintatapajono().getPrioriteetti();
                }
            }
        }
        return (huonoinJonosija == huonoimpienHakijaryhmistaHyvaksyttyjenJonosija(tamaJono, hakijaryhmaOids)
                && huonoinJonoPrioriteetti == tamaJono.getValintatapajono().getPrioriteetti());

    }

    private static int hakukohteenKiintionYlitysHakijaryhmalle(ValintatapajonoWrapper tutkittavaJono, String ryhmaOid) {
        String useOid = ryhmaOid != null ? ryhmaOid : ""; //ainakin testeissä näitä liikkuu nulleina, vaikka ei kai siinä järkeä ole
        int hyvaksyttyjaHakijaryhmasta = hakijaryhmastaHyvaksyttyjaHakukohteenKaikissaJonoissa(tutkittavaJono.getHakukohdeWrapper(), useOid);

        Optional<HakijaryhmaWrapper> relevantHW = tutkittavaJono.getHakukohdeWrapper().getHakijaryhmaWrappers().stream()
                .filter(w -> useOid.equals(w.getHakijaryhma().getOid())).findFirst();

        if (relevantHW.isPresent()) {
            int hakijaryhmaKiintio = relevantHW.get().getHakijaryhma().getKiintio();
            return hyvaksyttyjaHakijaryhmasta - hakijaryhmaKiintio;
        } else {
            return -1;
        }

    }

    private static int huonoimpienHakijaryhmistaHyvaksyttyjenJonosija(ValintatapajonoWrapper valintatapajonoWrapper, Set<String> hakijaryhmaOids) {
        Optional<HakemusWrapper> huonoin = valintatapajonoWrapper.getHakemukset()
                .stream()
                .filter(hw -> hw.getHakemus().getHyvaksyttyHakijaryhmista().containsAll(hakijaryhmaOids))
                .filter(HakemusWrapper::isHyvaksyttyHakijaryhmastaTallaKierroksella)
                .min((h1, h2) -> comparator.compare(h2, h1));
        return huonoin.isPresent() ? huonoin.get().getHakemus().getJonosija() : -1;
    }

    public String toString() {
        return ("HRS - Hakukohde " + hakukohdeOid + " - " +
                "valintatapajono " + valintatapajonoOid +
                ": " + valintatapajonoNimi +
                ". Tilaa (ilman mahdollisia lisäpaikkoja): " + tilaa +
                ", seuraavaksi hyväksyttäviä hakemuksia tasasijalla: " + seuraaviaTasasijalla +
                ". **Ehdolliset lisäpaikat:" +
                " (TAPA 1): " + ehdollisetAloituspaikatTapa1 +
                ", (TAPA 2): " + ehdollisetAloituspaikatTapa2 +
                ", jos jonosija parempi kuin: " + alinHakijaryhmaJonosija +
                ". Relevanteista ryhmistä " + alimpienHyvaksyttyjenHakijaryhmaOids + " hyväksyttyjä/kiintiöt: " + relevanteistaRyhmistaHyvaksyttyja + "/" + relevantitHakijaryhmakiintiot +
                ", Hyväksytyt (" + alimmallaSijallaHyvaksyttyja +
                " kpl, joista johonkin hakijaryhmään kuuluu " + alimmallaSijallaHakijaryhmastaHyvaksyttyja +
                ") alimmalla jonosijalla (" + alinHakijaryhmaJonosija + "): " + alimmallaSijallaOlevienTiedot +
                ". Ylitykset ryhmittäin: " + kiintionYlityksetRyhmittain +
                ". Hakemuksia jotka eivät kuulu hakijaryhmiin ja joilla on parempi jonosija: " + paremmatHakemuksetJaJonosijat);
    }

    public int lisapaikatKaytossa(int tapa) {
        if (kaikkiEhdot) {
            switch (tapa) {
                case 1: return ehdollisetAloituspaikatTapa1;
                case 2: return ehdollisetAloituspaikatTapa2;
            }
        }
        return 0;
    }

    public String toJsonString() {
        return "";
    }

    public boolean isKaikkiEhdot() {
        return kaikkiEhdot;
    }

}
