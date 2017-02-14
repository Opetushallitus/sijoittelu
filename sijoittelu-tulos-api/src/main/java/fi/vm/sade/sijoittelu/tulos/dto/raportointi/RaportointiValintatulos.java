package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.IlmoittautumisTila;

import java.util.Date;

public class RaportointiValintatulos {
    public final String hakemusOid;
    public final String valintatapajonoOid;
    public final boolean julkaistavissa;
    public final boolean ehdollisestiHyvaksyttavissa;
    public final boolean hyvaksyttyVarasijalta;
    public final Date viimeisinValintatuloksenMuutos;
    public final IlmoittautumisTila ilmoittautumisTila;
    public final String ehdollisenHyvaksymisenEhtoKoodi;

    public RaportointiValintatulos(String hakemusOid,
                                   String valintatapajonoOid,
                                   boolean julkaistavissa,
                                   boolean ehdollisestiHyvaksyttavissa,
                                   boolean hyvaksyttyVarasijalta,
                                   Date viimeisinValintatuloksenMuutos,
                                   IlmoittautumisTila ilmoittautumisTila,
                                   String ehdollisenHyvaksymisenEhtoKoodi) {
        this.hakemusOid = hakemusOid;
        this.valintatapajonoOid = valintatapajonoOid;
        this.julkaistavissa = julkaistavissa;
        this.ehdollisestiHyvaksyttavissa =  ehdollisestiHyvaksyttavissa;
        this.hyvaksyttyVarasijalta = hyvaksyttyVarasijalta;
        this.viimeisinValintatuloksenMuutos = viimeisinValintatuloksenMuutos;
        this.ilmoittautumisTila = ilmoittautumisTila;
        this.ehdollisenHyvaksymisenEhtoKoodi = ehdollisenHyvaksymisenEhtoKoodi;
    }
}
