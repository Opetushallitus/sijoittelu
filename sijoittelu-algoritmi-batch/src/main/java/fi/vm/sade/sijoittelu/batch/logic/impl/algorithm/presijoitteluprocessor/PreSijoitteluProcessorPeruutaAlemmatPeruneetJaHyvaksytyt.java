package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;

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

                boolean vastaanottanutSitovasti = isVastaanottanutSitovasti(valintatapajonoWrapper.getHakemukset(), hakukohdeWrapper.getHakukohde(),
                        valintatapajonoWrapper.getValintatapajono());
                for (HakemusWrapper hakemusWrapper : valintatapajonoWrapper.getHakemukset()) {
                    HenkiloWrapper henkiloWrapper = hakemusWrapper.getHenkilo();
                    Integer parasHyvaksyttyHakutoive = parasHyvaksyttyTaiPeruttuHakutoive(henkiloWrapper);

                    if (parasHyvaksyttyHakutoive != null && hakemusWrapper.isTilaVoidaanVaihtaa() &&
                        hakemusWrapper.getHakemus().getTila() == HakemuksenTila.VARALLA &&
                        hakemusWrapper.getHakemus().getPrioriteetti() > parasHyvaksyttyHakutoive ||
                        parasHyvaksyttyHakutoive != null && hakemusWrapper.isTilaVoidaanVaihtaa() &&
                        isVastaanottanutEhdollisesti(hakemusWrapper, hakukohdeWrapper.getHakukohde(),
                                        valintatapajonoWrapper.getValintatapajono()) &&
                        hakemusWrapper.getHakemus().getPrioriteetti() > parasHyvaksyttyHakutoive ||
                            vastaanottanutSitovasti && !isCurrentVastaanottanutSitovasti(hakemusWrapper, hakukohdeWrapper.getHakukohde(),
                                    valintatapajonoWrapper.getValintatapajono())
                    ) {
                        if (vastaanottanutSitovasti) {
                            hakemusWrapper.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikan());
                        } else {
                            hakemusWrapper.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutYlempiToive());
                        }
                        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                        hakemusWrapper.setTilaVoidaanVaihtaa(false);
                    }

                }

            }
        }
    }



    private boolean isVastaanottanutEhdollisesti(HakemusWrapper hakemusWrapper, Hakukohde hakukohde, Valintatapajono valintatapajono) {

        Valintatulos valintatulos = getValintatulos(hakukohde,valintatapajono,hakemusWrapper.getHakemus(),hakemusWrapper.getHenkilo().getValintatulos());
        if (valintatulos != null) {
            if (valintatulos.getTila() == ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT) {
                return true;
            }
        }
        return false;
    }

    private boolean isCurrentVastaanottanutSitovasti(HakemusWrapper hakemusWrapper, Hakukohde hakukohde, Valintatapajono valintatapajono) {

        Valintatulos valintatulos = getValintatulos(hakukohde,valintatapajono,hakemusWrapper.getHakemus(),hakemusWrapper.getHenkilo().getValintatulos());
        if (valintatulos != null) {
            if (valintatulos.getTila() == ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI) {
                return true;
            }
        }
        return false;
    }

    private boolean isVastaanottanutSitovasti(List<HakemusWrapper> hakemusWrapperit, Hakukohde hakukohde, Valintatapajono valintatapajono) {

        for(HakemusWrapper hakemusWrapper :  hakemusWrapperit) {
            Valintatulos valintatulos = getValintatulos(hakukohde,valintatapajono,hakemusWrapper.getHakemus(),hakemusWrapper.getHenkilo().getValintatulos());
            if (valintatulos != null) {
                if (valintatulos.getTila() == ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI) {
                    return true;
                }
            }
        }
        return false;
    }

    private Valintatulos getValintatulos(Hakukohde hakukohde, Valintatapajono valintatapajono, Hakemus hakemus, List<Valintatulos> valintatulokset) {
        if(valintatulokset != null) {
            for(Valintatulos vt : valintatulokset) {
                if(vt.getHakukohdeOid().equals(hakukohde.getOid()) &&
                        vt.getValintatapajonoOid().equals(valintatapajono.getOid()) ) {
                    if( vt.getHakemusOid() != null && !vt.getHakemusOid().isEmpty() && vt.getHakemusOid().equals(hakemus.getHakemusOid()) ) {
                        return vt;
                    }
                }
            }
        }
        return null;
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
