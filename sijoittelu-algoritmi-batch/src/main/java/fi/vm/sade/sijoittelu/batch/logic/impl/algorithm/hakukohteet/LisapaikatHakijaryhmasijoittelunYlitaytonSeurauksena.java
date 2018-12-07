package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.LisapaikkaTapa.*;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuHyvaksyttyihinTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods.hakemuksenTila;

public class LisapaikatHakijaryhmasijoittelunYlitaytonSeurauksena {
    private static HakemusWrapperComparator comparator = new HakemusWrapperComparator();

    //Apumuuttujat login ja jsonin parsimiseen
    final private String hakukohdeOid;
    final private String valintatapajonoOid;
    final private String valintatapajonoNimi;
    final private int tilaa;
    final private int seuraaviaTasasijalla;
    final private int alinHakijaryhmaJonosija;
    final private int relevanteistaRyhmistaHyvaksyttyja;
    final private int relevantitHakijaryhmakiintiot;
    final private int relevanttienKiintioidenYlitys;
    final private int alimmallaSijallaHyvaksyttyja;
    final private int alimmallaSijallaHakijaryhmastaHyvaksyttyja;
    final private List<String> alimmallaSijallaOlevienTiedot;
    final private int hakukohteessaHakijaryhmia;
    final private List<Pair<String, Integer>> kiintionYlityksetRyhmittain;
    final private List<Pair<String, Integer>> paremmatHakemuksetJaJonosijat;
    final private Set<String> alimpienHyvaksyttyjenHakijaryhmaOids = new HashSet<>();
    //--

    final private boolean kaikkiEhdot;
    final private int ehdollisetAloituspaikatTapa1;
    final private int ehdollisetAloituspaikatTapa2;
    final private List<HakemusWrapper> lisapaikoilleKelpaavat;


    LisapaikatHakijaryhmasijoittelunYlitaytonSeurauksena(ValintatapajonoWrapper valintatapajono, int tilaa, int seuraaviaTasasijalla, List<HakemusWrapper> valituiksiHaluavatHakemukset) {
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

        int hyvaksyttyja = 0;
        int kiintiot = 0;
        for (HakijaryhmaWrapper hw : relevantitHakijaryhmat) {
            kiintiot += hw.getHakijaryhma().getKiintio();
            hyvaksyttyja += hakijaryhmastaHyvaksyttyjaHakukohteenKaikissaJonoissa(valintatapajono.getHakukohdeWrapper(), hw.getHakijaryhma().getOid());
        }
        relevanteistaRyhmistaHyvaksyttyja = hyvaksyttyja;
        relevantitHakijaryhmakiintiot = kiintiot;
        relevanttienKiintioidenYlitys = hyvaksyttyja - kiintiot; //kiintiön ylitys tämän jonon viimeisellä sijalla olevien hakijaryhmissä

        this.alimmallaSijallaHyvaksyttyja = alimmallaSijallaOlevatHyvaksytyt.size();
        alimmallaSijallaHakijaryhmastaHyvaksyttyja = alimmallaSijallaOlevatHakijaryhmastaHyvaksytyt.size();

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
        lisapaikoilleKelpaavat = samallaTaiParemmallaJonosijallaOlevatHakijaryhmaanKuulumattomat(valintatapajono, valituiksiHaluavatHakemukset, alinHakijaryhmaJonosija);
        paremmatHakemuksetJaJonosijat = lisapaikoilleKelpaavat.stream()
                .map(hw -> Pair.of(hw.getHakemus().getHakemusOid(), hw.getHakemus().getJonosija()))
                .collect(Collectors.toList());

        alimmallaSijallaOlevienTiedot = alimmallaSijallaOlevatHyvaksytyt.stream().map(hw -> "(" + hw.getHakemus().getHakemusOid() + ", hyvaksyttyHakijaryhmista: " + hw.getHakemus().getHyvaksyttyHakijaryhmista() + ")").collect(Collectors.toList());
    }


    private static List<HakemusWrapper> samallaTaiParemmallaJonosijallaOlevatHakijaryhmaanKuulumattomat(ValintatapajonoWrapper jono, List<HakemusWrapper> valituiksiHaluavat, int jonosija) {
        return valituiksiHaluavat.stream()
                .filter(hw -> hw.getHakemus().getJonosija() <= jonosija)
                .filter(hw -> jono.getHakukohdeWrapper().getHakijaryhmaWrappers().stream()
                        .noneMatch(hrw -> hrw.getHakijaryhma().getHakemusOid().contains(hw.getHakemus().getHakemusOid())))
                .collect(Collectors.toList());
    }

    private static List<HakemusWrapper> alimmallaHyvaksytyllaJonosijallaOlevatHyvaksytyt(ValintatapajonoWrapper jono) {
        Optional<HakemusWrapper> hakemusHyvaksytyistaMatalimmallaJonosijalla = jono.getHakemukset().stream()
                .filter(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)))
                .min((h1, h2) -> comparator.compare(h2, h1));
        if (hakemusHyvaksytyistaMatalimmallaJonosijalla.isPresent()) {
            HakemusWrapper h = hakemusHyvaksytyistaMatalimmallaJonosijalla.get();
            int matalinJonosija = h.getHakemus().getJonosija();
            return jono.getHakemukset().stream()
                    .filter(hw -> hw.getHakemus().getJonosija() == matalinJonosija)
                    .filter(hw -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(hw)))
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

    public String toMinString() {
        return ("HRS - Hakukohde " + hakukohdeOid + " - " +
                "valintatapajono " + valintatapajonoOid +
                ": " + valintatapajonoNimi +
                ". **Ehdolliset lisäpaikat:" +
                " (TAPA 1): " + ehdollisetAloituspaikatTapa1 +
                ", (TAPA 2): " + ehdollisetAloituspaikatTapa2 +
                ". Hakemuksia jotka eivät kuulu hakijaryhmiin ja joilla on parempi jonosija: " + paremmatHakemuksetJaJonosijat);
    }

    public int getLisapaikat(LisapaikkaTapa tapa) {
        if (kaikkiEhdot) {
            switch (tapa) {
                case EI_KAYTOSSA: return 0;
                case TAPA1: return ehdollisetAloituspaikatTapa1;
                case TAPA2: return ehdollisetAloituspaikatTapa2;
            }
        }
        return 0;
    }

    public int minimiJonosija() {
        return alinHakijaryhmaJonosija;
    }

    public List<HakemusWrapper> lisapaikoilleKelpaavatHakemukset() {
        return lisapaikoilleKelpaavat;
    }

    public String toJsonString() {
        return "";
    }

    public boolean isKaikkiEhdot() {
        return kaikkiEhdot;
    }

}
