package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kjsaila on 04/11/14.
 */
public class TilanKuvaukset {

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
            put("FI","Peruuntunut, hyväksytty toisessa valintatapajonossa");
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
        }};
    }
}
