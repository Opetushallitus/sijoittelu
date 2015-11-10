package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

public class ParametriDTO {
    private ParametriArvoDTO PH_VTSSV; // Valintatulokset siirrettävä sijoitteluun viimeistään

    private ParametriArvoDTO PH_VSSAV; // Varasijasäännöt astuvat voimaan

    private ParametriArvoDTO PH_HKP; // Hakukierros päättyy

    private ParametriArvoDTO PH_VEH; // Valintaesitys hyväksyttävissä

    public ParametriDTO() {}

    public ParametriArvoDTO getPH_VTSSV() {
        return PH_VTSSV;
    }

    public void setPH_VTSSV(ParametriArvoDTO PH_VTSSV) {
        this.PH_VTSSV = PH_VTSSV;
    }

    public ParametriArvoDTO getPH_VSSAV() {
        return PH_VSSAV;
    }

    public void setPH_VSSAV(ParametriArvoDTO PH_VSSAV) {
        this.PH_VSSAV = PH_VSSAV;
    }

    public ParametriArvoDTO getPH_HKP() {
        return PH_HKP;
    }

    public void setPH_HKP(ParametriArvoDTO PH_HKP) {
        this.PH_HKP = PH_HKP;
    }

    public ParametriArvoDTO getPH_VEH() {
        return PH_VEH;
    }

    public void setPH_VEH(ParametriArvoDTO PH_VEH) {
        this.PH_VEH = PH_VEH;
    }
}
