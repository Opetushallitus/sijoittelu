package fi.vm.sade.sijoittelu.tulos.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.tulos.dao.impl.DAOImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.CRUD;
import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.READ;
import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.UPDATE;

/**
 *
 * @author Jussi Jartamo
 *
 */
@Path("export")
@Component
@PreAuthorize("isAuthenticated()")
public class SijoitteluntulosExportResource {

    // @Autowired
    // private DAO dao;

    // lets use same logic with export as with view
    @Autowired
    private DAOImpl dao;

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
    @Path("sijoitteluntulos.xls")
    @Secured({READ, UPDATE, CRUD})
    public Response exportSijoitteluntulos(@QueryParam("hakuOid") String hakuOid, @QueryParam("hakukohdeOid") String hakukohdeOid) {



        StringBuilder builder = new StringBuilder();

        Sijoittelu sijoittelu = dao.getSijoitteluByHakuOid(hakuOid);
        if(sijoittelu!=null) {
            SijoitteluAjo ajo = sijoittelu.getLatestSijoitteluajo();
            if(ajo != null) {
            //    System.out.println(ajo.getSijoitteluajoId() + "<>" + hakukohdeOid);
                Hakukohde hakukohde = dao.getHakukohdeBySijoitteluajo(ajo.getSijoitteluajoId(), hakukohdeOid);
                //  System.out.println("JORMA! " + hakukohde);
                // getHakukohdeBySijoitteluajo

                builder.append("<table>");

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

                builder.append("</table>");
            }
        }

        return Response.ok(builder.toString())
                .header("Content-Disposition", "attachment; filename*=UTF-8''sijoitteluntulos.xls;").build();
    }

}
