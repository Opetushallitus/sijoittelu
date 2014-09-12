package fi.vm.sade.sijoittelu.domain;

public class ValintatulosPerustiedot {
    public final String hakuOid;
    public final String hakukohdeOid;
    public final String valintatapajonoOid;
    public final String hakemusOid;
    public final String hakijaOid;
    public final int hakutoiveenPrioriteetti;

    public ValintatulosPerustiedot(final String hakuOid, final String hakukohdeOid, final String valintatapajonoOid, final String hakemusOid, final String hakijaOid, final int hakutoiveenPrioriteetti) {
        this.hakuOid = hakuOid;
        this.hakukohdeOid = hakukohdeOid;
        this.valintatapajonoOid = valintatapajonoOid;
        this.hakemusOid = hakemusOid;
        this.hakijaOid = hakijaOid;
        this.hakutoiveenPrioriteetti = hakutoiveenPrioriteetti;
    }

    public Valintatulos createValintatulos(ValintatuloksenTila tila) {
        final Valintatulos valintatulos = new Valintatulos();
        valintatulos.setHakemusOid(hakemusOid);
        valintatulos.setHakijaOid(hakijaOid);
        valintatulos.setHakukohdeOid(hakukohdeOid);
        valintatulos.setHakuOid(hakuOid);
        valintatulos.setHakutoive(hakutoiveenPrioriteetti);
        valintatulos.setIlmoittautumisTila(IlmoittautumisTila.EI_ILMOITTAUTUNUT);
        valintatulos.setTila(tila);
        valintatulos.setValintatapajonoOid(valintatapajonoOid);
        return valintatulos;
    }
}
