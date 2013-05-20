package fi.vm.sade.sijoittelu.tulos.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;

/**
 * 
 * @author Jussi Jartamo
 * 
 */
@Path("/export")
@Component
public class SijoitteluntulosExportResource {

    // @Autowired
    // private DAO dao;

    // lets use same logic with export as with view
    @Autowired
    private SijoitteluResource sijoitteluResource;

    // @Autowired
    // private SijoitteluajoResource sijoitteluajoResource;

    /*
     * SijoitteluajoHakukohde.get({ sijoitteluajoOid: currentSijoitteluajoOid,
     * hakukohdeOid: currentHakukohdeOid } <table class="virkailija-table-1"
     * ng-repeat="jono in model.sijoitteluTulokset.valintatapajonot">
     * <h1>{{jono.aloituspaikat}}</h1> <thead> <tr> <th
     * colspan="6">valintatapajono OID: {{jono.oid}}</th> </tr> <tr> <td
     * class="bold align-center">jonosija</td> <td
     * class="bold align-center">Hakija OID</td> <td
     * class="bold align-center">Hakemus OID</td> <td
     * class="bold align-center">prioriteetti</td> <td
     * class="bold align-center">tasasijaJonosija</td> <td
     * class="bold align-center">tila</td> </tr> </thead> <tr
     * ng-repeat="hakemus in jono.hakemukset"> <td>{{hakemus.jonosija}}</td>
     * <td>{{hakemus.hakijaOid}}</td> <td>{{hakemus.hakemusOid}}</td>
     * <td>{{hakemus.prioriteetti}}</td> <td>{{hakemus.tasasijaJonosija}}</td>
     * <td>{{hakemus.tila}}</td> </tr>
     */
    @GET
    @Path("/sijoitteluntulos.xls")
    public String exportSijoitteluntulos(@QueryParam("hakuOid") String hakuOid) {
        // getHakukohdeBySijoitteluajo
        StringBuilder builder = new StringBuilder();
        builder.append("<table>");
        for (SijoitteluAjo ajo : sijoitteluResource.getSijoitteluajoByHakuOid(hakuOid, true)) {

            Long sijoitteluajoId = ajo.getSijoitteluajoId();
            for (HakukohdeItem hakukohdeItem : ajo.getHakukohteet()) {
                Hakukohde hakukohde = hakukohdeItem.getHakukohde();
                for (Valintatapajono jono : hakukohde.getValintatapajonot()) {
                    builder.append("<tr>");
                    builder.append("<td colspan=\"6\">Jono ").append(jono.getOid()).append("</td>");
                    builder.append("</tr>");
                    for (Hakemus hakemus : jono.getHakemukset()) {
                        builder.append("<tr>");
                        builder.append("<td>").append(hakemus.getJonosija()).append("</td>");
                        builder.append("<td>").append(hakemus.getHakijaOid()).append("</td>");
                        builder.append("<td>").append(hakemus.getPrioriteetti()).append("</td>");
                        builder.append("<td>").append(hakemus.getTasasijaJonosija()).append("</td>");
                        builder.append("<td>").append(hakemus.getTila()).append("</td>");
                        builder.append("</tr>");
                    }
                }
                // sijoitteluajoResource.getHakukohdeBySijoitteluajo(sijoitteluajoId,
                // hakukohde.getOid());

            }
        }
        builder.append("</table>");
        return builder.toString();
    }

}
