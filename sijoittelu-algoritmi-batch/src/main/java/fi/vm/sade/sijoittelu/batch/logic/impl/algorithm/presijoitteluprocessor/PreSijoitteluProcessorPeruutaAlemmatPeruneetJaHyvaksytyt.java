package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;

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
        for(HakukohdeWrapper hakukohdeWrapper : sijoitteluajoWrapper.getHakukohteet()) {
            for(ValintatapajonoWrapper valintatapajonoWrapper : hakukohdeWrapper.getValintatapajonot()) {
                for(HakemusWrapper hakemusWrapper : valintatapajonoWrapper.getHakemukset()) {
                    HenkiloWrapper henkiloWrapper = hakemusWrapper.getHenkilo();
                    Integer parasHyvaksyttyHakutoive = parasHyvaksyttyTaiPeruttuHakutoive(henkiloWrapper);
                    if(parasHyvaksyttyHakutoive != null) {
                        if(hakemusWrapper.isTilaVoidaanVaihtaa() && hakemusWrapper.getHakemus().getTila() == HakemuksenTila.VARALLA && hakemusWrapper.getHakemus().getPrioriteetti() >= parasHyvaksyttyHakutoive) {
                            hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                        }
                    }
                }
            }
        }
    }

    private Integer parasHyvaksyttyTaiPeruttuHakutoive(HenkiloWrapper wrapper) {
        Integer parasHyvaksyttyHakutoive = null;
        for(HakemusWrapper hakemusWrapper :  wrapper.getHakemukset()) {
            if(hakemusWrapper.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY || hakemusWrapper.getHakemus().getTila() == HakemuksenTila.PERUNUT) {
                if(parasHyvaksyttyHakutoive == null || parasHyvaksyttyHakutoive > hakemusWrapper.getHakemus().getPrioriteetti())  {
                    parasHyvaksyttyHakutoive = hakemusWrapper.getHakemus().getPrioriteetti();
                }
            }
        }
        return parasHyvaksyttyHakutoive;
    }

}
