package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;

public class WrapperHelperMethods {

    public static String hakemuksenHakemusOid(HakemusWrapper h) {
        return h.getHakemus().getHakemusOid();
    }

    public static HakemuksenTila hakemuksenTila(HakemusWrapper h) {
        return h.getHakemus().getTila();
    }

    public static Tasasijasaanto jononTasasijasaanto(ValintatapajonoWrapper valintatapajonoWrapper) {
        return valintatapajonoWrapper.getValintatapajono().getTasasijasaanto();
    }

    public static Integer jononPrioriteetti(HakemusWrapper h) {
        return h.getValintatapajono().getValintatapajono().getPrioriteetti();
    }

    public static Integer jononAloituspaikat(ValintatapajonoWrapper valintatapajono) {
        return valintatapajono.getValintatapajono().getAloituspaikat();
    }

    public static Integer hakemuksenPrioriteetti(HakemusWrapper hakemusWrapper) {
        return hakemusWrapper.getHakemus().getPrioriteetti();
    }

    public static Boolean jononEiVarasijatayttoa(HakemusWrapper hakemusWrapper) {
        return hakemusWrapper.getValintatapajono().getValintatapajono().getEiVarasijatayttoa();
    }

    public static Boolean jononKaikkiEhdonTayttavatHyvaksytaan(HakemusWrapper hakemusWrapper) {
        return hakemusWrapper.getValintatapajono().getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan();
    }

    public static boolean siirtynytToisestaValintatapajonosta(HakemusWrapper hakemusWrapper) {
        return hakemusWrapper.getHakemus().getSiirtynytToisestaValintatapajonosta();
    }

    public static void asetaSiirtynytToisestaValintatapajonosta(HakemusWrapper hakemusWrapper, boolean b) {
        hakemusWrapper.getHakemus().setSiirtynytToisestaValintatapajonosta(b);
    }
}
