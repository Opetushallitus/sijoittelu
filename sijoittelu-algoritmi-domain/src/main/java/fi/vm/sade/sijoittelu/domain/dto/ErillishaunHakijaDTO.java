package fi.vm.sade.sijoittelu.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

import java.util.Date;
import java.util.Optional;

@ApiModel("Erillishaunhakija")
public class ErillishaunHakijaDTO {
    @ApiModelProperty(value = "Valintatapajonon tunniste", required = true)
    public String valintatapajonoOid;
    @ApiModelProperty(value = "Hakemuksen tunniste", required = false)
    public String hakemusOid;
    @ApiModelProperty(value = "Hakukohteen tunniste", required = false)
    public String hakukohdeOid;
    @ApiModelProperty(value = "Saako hakijan tulokset julkaista", required = false)
    public boolean julkaistavissa;
    //public boolean hyvaksyttyVarasijalta;
    @ApiModelProperty(value = "Hakijan tunniste", required = false)
    public String hakijaOid;
    @ApiModelProperty(value = "Haun tunniste", required = false)
    public String hakuOid;
    @ApiModelProperty(value = "Tarjoajan (organisaation) tunniste", required = false)
    public String tarjoajaOid;
    //public int hakutoive; // aina ensimmainen?
    @ApiModelProperty(value = "Valintatuloksen tila", required = false)
    public ValintatuloksenTila valintatuloksenTila;
    @ApiModelProperty(value = "Ehdollinen valinta", required = false)
    public boolean ehdollisestiHyvaksyttavissa;
    @ApiModelProperty(value = "Ilmoittautumisen tila", required = false)
    public IlmoittautumisTila ilmoittautumisTila;
    @ApiModelProperty(value = "Hakemuksen tila", required = false)
    public HakemuksenTila hakemuksenTila;
    @ApiModelProperty(value = "Hakijan etunimi", required = false)
    public String etunimi;
    @ApiModelProperty(value = "Hakijan sukunimi", required = false)
    public String sukunimi;
    @ApiModelProperty(value = "Hyväksymiskirje lähetetty hakijalle", required = false)
    public Date hyvaksymiskirjeLahetetty;

    public boolean poistetaankoTulokset = false;

    public ErillishaunHakijaDTO(
            String              valintatapajonoOid,
            String              hakemusOid,
            String              hakukohdeOid,
            boolean             julkaistavissa,
            String              hakijaOid,
            String              hakuOid,
            String              tarjoajaOid,
            ValintatuloksenTila valintatuloksenTila,
            boolean             ehdollisestiHyvaksyttavissa,
            IlmoittautumisTila  ilmoittautumisTila,
            HakemuksenTila      hakemuksenTila,
            String              etunimi,
            String              sukunimi,
            Optional<Boolean>   poistetaankoTulokset,
            Date                hyvaksymiskirjeLahetetty) {

        this.valintatapajonoOid         = valintatapajonoOid;
        this.hakemusOid                 = hakemusOid;
        this.hakukohdeOid               = hakukohdeOid;
        this.julkaistavissa             = julkaistavissa;
        this.hakijaOid                  = hakijaOid;
        this.hakuOid                    = hakuOid;
        this.tarjoajaOid                = tarjoajaOid;
        this.valintatuloksenTila        = valintatuloksenTila;
        this.ehdollisestiHyvaksyttavissa = ehdollisestiHyvaksyttavissa;
        this.ilmoittautumisTila         = ilmoittautumisTila;
        this.hakemuksenTila             = hakemuksenTila;
        this.etunimi                    = etunimi;
        this.sukunimi                   = sukunimi;
        this.poistetaankoTulokset       = poistetaankoTulokset.orElse(false);
        this.hyvaksymiskirjeLahetetty   = hyvaksymiskirjeLahetetty;
    }

    public ErillishaunHakijaDTO() {
    }

    public String getValintatapajonoOid() {
        return valintatapajonoOid;
    }

    public void setValintatapajonoOid(String valintatapajonoOid) {
        this.valintatapajonoOid = valintatapajonoOid;
    }

    public String getHakemusOid() {
        return hakemusOid;
    }

    public void setHakemusOid(String hakemusOid) {
        this.hakemusOid = hakemusOid;
    }

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public void setHakukohdeOid(String hakukohdeOid) {
        this.hakukohdeOid = hakukohdeOid;
    }

    public boolean isJulkaistavissa() {
        return julkaistavissa;
    }

    public void setJulkaistavissa(boolean julkaistavissa) {
        this.julkaistavissa = julkaistavissa;
    }

    public String getHakijaOid() {
        return hakijaOid;
    }

    public void setHakijaOid(String hakijaOid) {
        this.hakijaOid = hakijaOid;
    }

    public String getHakuOid() {
        return hakuOid;
    }

    public void setHakuOid(String hakuOid) {
        this.hakuOid = hakuOid;
    }

    public String getTarjoajaOid() {
        return tarjoajaOid;
    }

    public void setTarjoajaOid(String tarjoajaOid) {
        this.tarjoajaOid = tarjoajaOid;
    }

    public ValintatuloksenTila getValintatuloksenTila() {
        return valintatuloksenTila;
    }

    public void setValintatuloksenTila(ValintatuloksenTila valintatuloksenTila) {
        this.valintatuloksenTila = valintatuloksenTila;
    }

    public IlmoittautumisTila getIlmoittautumisTila() {
        return ilmoittautumisTila;
    }

    public void setIlmoittautumisTila(IlmoittautumisTila ilmoittautumisTila) {
        this.ilmoittautumisTila = ilmoittautumisTila;
    }

    public HakemuksenTila getHakemuksenTila() {
        return hakemuksenTila;
    }

    public void setHakemuksenTila(HakemuksenTila hakemuksenTila) {
        this.hakemuksenTila = hakemuksenTila;
    }

    public String getEtunimi() {
        return etunimi;
    }

    public void setEtunimi(String etunimi) {
        this.etunimi = etunimi;
    }

    public String getSukunimi() {
        return sukunimi;
    }

    public void setSukunimi(String sukunimi) {
        this.sukunimi = sukunimi;
    }

    public boolean getPoistetaankoTulokset() {
        return this.poistetaankoTulokset;
    }

    public void setPoistetaankoTulokset(boolean poistetaankoTulokset) {
        this.poistetaankoTulokset = poistetaankoTulokset;
    }

    public boolean isEhdollisestiHyvaksyttavissa() {
        return ehdollisestiHyvaksyttavissa;
    }

    public void setEhdollisestiHyvaksyttavissa(boolean ehdollisestiHyvaksyttavissa) {
        this.ehdollisestiHyvaksyttavissa = ehdollisestiHyvaksyttavissa;
    }

    public Date getHyvaksymiskirjeLahetetty() {
        return hyvaksymiskirjeLahetetty;
    }

    public void setHyvaksymiskirjeLahetetty(Date hyvaksymiskirjeLahetetty) {
        this.hyvaksymiskirjeLahetetty = hyvaksymiskirjeLahetetty;
    }

    public Valintatulos asValintatulos() {
        return new Valintatulos(
                hakemusOid,
                hakijaOid,
                hakukohdeOid,
                hakuOid,
                1,
                HakemuksenTila.VARASIJALTA_HYVAKSYTTY == hakemuksenTila,
                ilmoittautumisTila,
                julkaistavissa,
                valintatuloksenTila,
                ehdollisestiHyvaksyttavissa,
                valintatapajonoOid,
                hyvaksymiskirjeLahetetty);
    }
}
