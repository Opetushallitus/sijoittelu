package fi.vm.sade.sijoittelu.laskenta.resource;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValintatulosUpdateStatus {
    public final int status;
    public final String message;
    public final String valintatapajonoOid;
    public final String hakemusOid;

    public ValintatulosUpdateStatus(int status, String message, String valintatapajonoOid, String hakemusOid) {
        this.status = status;
        this.message = message;
        this.valintatapajonoOid = valintatapajonoOid;
        this.hakemusOid = hakemusOid;
    }
}
