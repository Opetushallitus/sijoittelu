package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import fi.vm.sade.sijoittelu.domain.Hakemus;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class HakemusWrapper {

    private Hakemus hakemus;

    private ValintatapajonoWrapper valintatapajono;

    private HenkiloWrapper henkilo;

    private boolean hyvaksyttyHakijaryhmastaTaiTayttoJonosta = false;

    private boolean hyvaksyttavissaHakijaryhmanJalkeen = false;

    //jos hakemuksen tilaa ei voida muuttaa, esm. ilmoitettu hakijalle jo
    private boolean tilaVoidaanVaihtaa = true;

    public HenkiloWrapper getHenkilo() {
        return henkilo;
    }

    public void setHenkilo(HenkiloWrapper henkilo) {
        this.henkilo = henkilo;
    }

    public Hakemus getHakemus() {
        return hakemus;
    }

    public void setHakemus(Hakemus hakemus) {
        this.hakemus = hakemus;
    }

    public ValintatapajonoWrapper getValintatapajono() {
        return valintatapajono;
    }

    public void setValintatapajono(ValintatapajonoWrapper valintatapajono) {
        this.valintatapajono = valintatapajono;
    }


    public boolean isTilaVoidaanVaihtaa() {
        return tilaVoidaanVaihtaa;
    }

    public void setTilaVoidaanVaihtaa(boolean tilaVoidaanVaihtaa) {
        this.tilaVoidaanVaihtaa = tilaVoidaanVaihtaa;
    }

    public boolean isHyvaksyttyHakijaryhmastaTaiTayttoJonosta() {
        return hyvaksyttyHakijaryhmastaTaiTayttoJonosta;
    }

    public void setHyvaksyttyHakijaryhmastaTaiTayttoJonosta(boolean hyvaksyttyHakijaryhmastaTaiTayttoJonosta) {
        this.hyvaksyttyHakijaryhmastaTaiTayttoJonosta = hyvaksyttyHakijaryhmastaTaiTayttoJonosta;
    }

    public boolean isHyvaksyttavissaHakijaryhmanJalkeen() {
        return hyvaksyttavissaHakijaryhmanJalkeen;
    }

    public void setHyvaksyttavissaHakijaryhmanJalkeen(boolean hyvaksyttavissaHakijaryhmanJalkeen) {
        this.hyvaksyttavissaHakijaryhmanJalkeen = hyvaksyttavissaHakijaryhmanJalkeen;
    }
}
