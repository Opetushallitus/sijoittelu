package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import java.util.HashMap;
import java.util.Map;

public class TilanKuvaukset {
    public static Map<String, String> tyhja = new HashMap<>();

    public static Map<String, String> peruuntunutYlempiToive() {
        return new HashMap<String, String>() {{
            put("FI", "Peruuntunut, hyväksytty ylemmälle hakutoiveelle");
            put("SV", "Annullerad, godkänt till ansökningsmål med högre prioritet");
            put("EN", "Cancelled, accepted for a study place with higher priority");
        }};
    }

    public static Map<String, String> peruuntunutAloituspaikatTaynna() {
        return new HashMap<String, String>() {{
            put("FI", "Peruuntunut, aloituspaikat täynnä");
            put("SV", "Annullerad, nybörjarplatser fyllda");
            put("EN", "Cancelled, study places are filled");
        }};
    }

    public static Map<String, String> peruuntunutHyvaksyttyToisessaJonossa() {
        return new HashMap<String, String>() {{
            put("FI", "Peruuntunut, hyväksytty toisessa valintatapajonossa");
            put("SV", "Annullerad, godkänd i en annan urvalsmetodskö");
            put("EN", "Cancelled, accepted in another selection method queue");
        }};
    }

    public static Map<String, String> varasijaltaHyvaksytty() {
        return new HashMap<String, String>() {{
            put("FI", "Varasijalta hyväksytty");
            put("SV", "Godkänd från reservplats");
            put("EN", "Accepted from a reserve place");
        }};
    }

    public static Map<String, String> peruuntunutEiVastaanottanutMaaraaikana() {
        return new HashMap<String, String>() {{
            put("FI", "Peruuntunut, ei vastaanottanut määräaikana");
            put("SV", "Annullerad, har inte tagit emot platsen inom utsatt tid");
            put("EN", "Cancelled, has not confirmed the study place within the deadline");
        }};
    }

    public static Map<String, String> peruuntunutVastaanottanutToisenOpiskelupaikan() {
        return new HashMap<String, String>() {{
            put("FI", "Peruuntunut, ottanut vastaan toisen opiskelupaikan");
            put("SV", "Annullerad, ottanut vastaan toisen opiskelupaikan");
            put("EN", "Cancelled, ottanut vastaan toisen opiskelupaikan");
        }};
    }

    public static Map<String, String> peruuntunutEiMahduKasiteltavienVarasijojenMaaraan() {
        return new HashMap<String, String>() {{
            put("FI", "Peruuntunut, varasija ei mahdu käsiteltävien varasijojen määrään");
            put("SV", "Annullerad, reservplatsen ryms inte med i antalet reservplatser");
            put("EN", "Cancelled, the reserve place does not fit into the amount of processed reserve places");
        }};
    }

    public static Map<String, String> peruuntunutHakukierrosOnPaattynyt() {
        return new HashMap<String, String>() {{
            put("FI", "Peruuntunut, varasijatäyttö päättynyt");
            put("SV", "Annullerad, besättning av reservplatser har upphört");
            put("EN", "Cancelled, reserve place selection has ended");
        }};
    }

    public static Map<String, String> peruuntunutEiVarasijaTayttoa() {
        return new HashMap<String, String>() {{
            put("FI", "Peruuntunut, ei varasijatäyttöä");
            put("SV", "Annullerad, ingen antagning från reservplats");
            put("EN", "Cancelled, no reserve place practice");
        }};
    }

    public static Map<String, String> hyvaksyttyTayttojonoSaannolla(String jono) {
        return new HashMap<String, String>() {{
            put("FI", "Hyväksytty täyttöjonosäännöllä valintatapajonosta: " + jono);
            put("SV", "Godkänd med köpåfyllningsregel från urvalsmetodskö: " + jono);
            put("EN", "Accepted from selection method queue: " + jono);
        }};
    }

    public static Map<String, String> hylattyHakijaryhmaanKuulumattomana(String hakijaryhma) {
        return new HashMap<String, String>() {{
            put("FI", "Hylätty, ei kuulu hakijaryhmään: " + hakijaryhma);
            put("SV", "Annullerad, hör inte till gruppen för sökande: " + hakijaryhma);
            put("EN", "Cancelled, is not included in the applicant group: " + hakijaryhma);
        }};
    }

    public static Map<String,String> peruuntunutVastaanottanutToisenOpiskelupaikanYhdenPaikanSaannonPiirissa() {
        return new HashMap<String, String>() {{
            put("FI", "Peruuntunut, ottanut vastaan toisen opiskelupaikan yhden paikan säännön piirissä");
            put("SV", "Annullerad, ottanut vastaan toisen opiskelupaikan yhden paikan säännön piirissä");
            put("EN", "Cancelled, ottanut vastaan toisen opiskelupaikan yhden paikan säännön piirissä");
        }};
    }
}
