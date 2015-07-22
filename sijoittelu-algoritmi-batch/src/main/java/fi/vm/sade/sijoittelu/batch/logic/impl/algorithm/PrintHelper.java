package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.valintalaskenta.domain.dto.HakijaDTO;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValinnanvaiheDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;

import java.util.HashSet;
import java.util.Set;

public final class PrintHelper {

    private PrintHelper() {
    }

    public final static String tulostaSijoittelu(SijoitteluAlgorithm a) {
        SijoitteluajoWrapper s = ((SijoitteluAlgorithmImpl) a).sijoitteluAjo;
        StringBuilder sb = new StringBuilder();
        Set<String> henkilot = new HashSet<String>();
        int hakemukset = 0;
        int valintatapajonot = 0;
        int hakukohteet = 0;
        for (HakukohdeWrapper hki : s.getHakukohteet()) {
            hakukohteet++;
            Hakukohde hk = hki.getHakukohde();
            for (Valintatapajono vt : hk.getValintatapajonot()) {
                valintatapajonot++;
                for (Hakemus h : vt.getHakemukset()) {
                    hakemukset++;
                    henkilot.add(h.getHakemusOid());
                }
            }
        }
        sb.append("===================================================\n");
        sb.append("Summary:\n");
        sb.append("Hakukohteet:      " + hakukohteet + "\n");
        sb.append("Valintatapajonot: " + valintatapajonot + "\n");
        sb.append("Hakijat:          " + henkilot.size() + "\n");
        sb.append("Hakemukset:       " + hakemukset + "\n");
        sb.append("Rekursio syvyys:  " + ((SijoitteluAlgorithmImpl) a).depth + "\n");
        sb.append("===================================================\n");
        for (HakukohdeWrapper hki : s.getHakukohteet()) {
            sb.append("HAKUKOHDE: [" + hki.getHakukohde().getOid() + "]\n");
            for (ValintatapajonoWrapper jono : hki.getValintatapajonot()) {
                int hyvaksytty = 0;
                for (HakemusWrapper hakemus : jono.getHakemukset()) {
                    if (hakemus.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY) {
                        hyvaksytty++;
                    }
                }
                sb.append("  JONO [" + jono.getValintatapajono().getOid() + "], prioriteetti [" + jono.getValintatapajono().getPrioriteetti() + "] aloituspaikat [" + jono.getValintatapajono().getAloituspaikat() + "], hyvaksytty[" + hyvaksytty + "] tasasijasaanto [" + jono.getValintatapajono().getTasasijasaanto() + "]\n");
                for (HakemusWrapper hakemus : jono.getHakemukset()) {
                    sb.append("          " + hakemus.getHakemus().getJonosija() + "." + hakemus.getHakemus().getTasasijaJonosija() + "  " + hakemus.getHakemus().getHakemusOid() + " " + hakemus.getHakemus().getTila() + " hakijan prijo:" + hakemus.getHakemus().getPrioriteetti() + " muuttunut sijoittelussa = " + hakemus.getHakemus().isOnkoMuuttunutViimeSijoittelussa() + "\n");
                }
            }
        }
        sb.append("===================================================\n");
        return sb.toString();
    }

    public final static String tulostaSijoittelu(HakuDTO s) {
        StringBuilder sb = new StringBuilder();
        sb.append("===================================================\n");
        sb.append("Haku: " + s.getHakuOid() + "\n");
        sb.append("===================================================\n");
        for (HakukohdeDTO hki : s.getHakukohteet()) {
            sb.append("HAKUKOHDE: [" + hki.getOid() + "]\n");
            for (ValintatietoValinnanvaiheDTO v : hki.getValinnanvaihe()) {

                for (ValintatietoValintatapajonoDTO jono : v.getValintatapajonot()) {
                    sb.append("  JONO [" + jono.getOid() + "], prioriteetti [" + jono.getPrioriteetti() + "] aloituspaikat [" + jono.getAloituspaikat() + "], tasasijasaanto [" + jono.getTasasijasaanto() + "]\n");
                    for (HakijaDTO hakemus : jono.getHakija()) {
                        sb.append("          " + hakemus.getJonosija() + "." + "  " + hakemus.getOid() + " " + hakemus.getHakemusOid() + " " + hakemus.getTila() + " hakijan prijo:" + hakemus.getPrioriteetti() + "\n");
                    }
                }
            }
        }
        sb.append("===================================================\n");
        return sb.toString();
    }
}