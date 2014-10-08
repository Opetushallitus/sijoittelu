package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 14.11.2013
 * Time: 9:03
 * To change this template use File | Settings | File Templates.
 */
public class PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt implements PreSijoitteluProcessor {
    /**
     * Peruutetaan jo jonnekkin hyväksyttyjen alemmat hakemukset.
     * Tata voisi optimoida bitusti luuppaamalla vain henkilo=>hakemus tyyliin. mutta kiire.
     * Kaytannossa tata tarvitaan jos algoritmi on tyytyvainen nykytilanteeseeen eika koskaan muuta henkilon tilaa, jolloin varalla olevat jaavat koskemattomiksi.
     * @param sijoitteluajoWrapper
     */

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        for (HakukohdeWrapper hakukohdeWrapper : sijoitteluajoWrapper.getHakukohteet()) {
            for (ValintatapajonoWrapper valintatapajonoWrapper : hakukohdeWrapper.getValintatapajonot()) {

                boolean vastaanottanutSitovasti = isVastaanottanutSitovasti(valintatapajonoWrapper.getHakemukset());
                for (HakemusWrapper hakemusWrapper : valintatapajonoWrapper.getHakemukset()) {
                    HenkiloWrapper henkiloWrapper = hakemusWrapper.getHenkilo();
                    Integer parasHyvaksyttyHakutoive = parasHyvaksyttyTaiPeruttuHakutoive(henkiloWrapper);

                    if (parasHyvaksyttyHakutoive != null && hakemusWrapper.isTilaVoidaanVaihtaa() &&
                        hakemusWrapper.getHakemus().getTila() == HakemuksenTila.VARALLA &&
                        hakemusWrapper.getHakemus().getPrioriteetti() >= parasHyvaksyttyHakutoive ||
                        hakemusWrapper.isTilaVoidaanVaihtaa() && vastaanottanutSitovasti
                    ) {

                        hakemusWrapper.getHakemus().getTilanKuvaukset().put("FI","Peruuntunut, hyväksytty ylemmälle hakutoiveelle");
                        hakemusWrapper.getHakemus().getTilanKuvaukset().put("SV","Annullerad, godkänt till ansökningsmål med högre prioritet");
                        hakemusWrapper.getHakemus().getTilanKuvaukset().put("EN","Cancelled, accepted for a study place with higher priority");
                        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                    }

                }



            }
        }
    }

    private boolean isVastaanottanutSitovasti(List<HakemusWrapper> hakemusWrapperit) {
        for(HakemusWrapper hakemusWrapper :  hakemusWrapperit) {
            if(hakemusWrapper.getHenkilo().getValintatulos().get(0).getTila() == ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI) {
                return true;
            }
        }
        return false;
    }

    private Integer parasHyvaksyttyTaiPeruttuHakutoive(HenkiloWrapper wrapper) {
        Integer parasHyvaksyttyHakutoive = null;
        for(HakemusWrapper hakemusWrapper :  wrapper.getHakemukset()) {
            if(hakemusWrapper.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY
                    || hakemusWrapper.getHakemus().getTila() == HakemuksenTila.PERUNUT
                    || hakemusWrapper.getHakemus().getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                if(parasHyvaksyttyHakutoive == null || parasHyvaksyttyHakutoive > hakemusWrapper.getHakemus().getPrioriteetti())  {
                    parasHyvaksyttyHakutoive = hakemusWrapper.getHakemus().getPrioriteetti();
                }
            }
        }
        return parasHyvaksyttyHakutoive;
    }

}
