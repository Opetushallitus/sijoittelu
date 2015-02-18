package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;

/**
 * Created by kjsaila on 18/02/15.
 */
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

}
