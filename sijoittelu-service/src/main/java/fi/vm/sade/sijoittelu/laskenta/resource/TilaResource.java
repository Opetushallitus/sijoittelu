package fi.vm.sade.sijoittelu.laskenta.resource;

import static fi.vm.sade.sijoittelu.laskenta.roles.SijoitteluRole.READ_UPDATE_CRUD;
import static fi.vm.sade.sijoittelu.laskenta.roles.SijoitteluRole.UPDATE_CRUD;

import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.dto.ErillishaunHakijaDTO;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import org.springframework.stereotype.Controller;
import scala.tools.cmd.Opt;
import static fi.vm.sade.sijoittelu.laskenta.util.SijoitteluAudit.*;
import static fi.vm.sade.auditlog.LogMessage.builder;

@Controller
@Path("tila")
@PreAuthorize("isAuthenticated()")
@Api(value = "/tila", description = "Resurssi sijoittelun tilojen käsittelyyn")
public class TilaResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(TilaResource.class);

    static final String LATEST = "latest";

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;

    @Autowired
    private RaportointiService raportointiService;

    @Autowired
    private HakukohdeDao hakukohdeDao;

    @Autowired
    private SijoitteluDao sijoitteluDao;

    @Autowired
    private ValintatulosDao valintatulosDao;

    @Autowired
    TarjontaIntegrationService tarjontaIntegrationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakemusOid}")
    @PreAuthorize(READ_UPDATE_CRUD)
    @ApiOperation(value = "Hakemuksen valintatulosten haku")
    public List<Valintatulos> hakemus(@PathParam("hakemusOid") String hakemusOid) {
        List<Valintatulos> v = sijoitteluBusinessService.haeHakemuksenTila(hakemusOid);
        if (v == null) {
            v = new ArrayList<>();
        }
        return v;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakemusOid}/{hakuOid}/{hakukohdeOid}/{valintatapajonoOid}/")
    @PreAuthorize(READ_UPDATE_CRUD)
    @ApiOperation(value = "Hakemuksen valintatulosten haku tietyssä hakukohteessa ja valintatapajonossa")
    public Valintatulos hakemus(@PathParam("hakuOid") String hakuOid,
                                @PathParam("hakukohdeOid") String hakukohdeOid,
                                @PathParam("valintatapajonoOid") String valintatapajonoOid,
                                @PathParam("hakemusOid") String hakemusOid) {
        Valintatulos v = sijoitteluBusinessService.haeHakemuksenTila(hakuOid, hakukohdeOid, valintatapajonoOid, hakemusOid);
        if (v == null) {
            v = new Valintatulos();
        }
        return v;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/hakukohde/{hakukohdeOid}/{valintatapajonoOid}/")
    @PreAuthorize(READ_UPDATE_CRUD)
    @ApiOperation(value = "Valintatulosten haku hakukohteelle ja valintatapajonolle")
    public List<Valintatulos> haku(
            @PathParam("hakukohdeOid") String hakukohdeOid,
            @PathParam("valintatapajonoOid") String valintatapajonoOid) {
        List<Valintatulos> v = sijoitteluBusinessService.haeHakemustenTilat(hakukohdeOid, valintatapajonoOid);
        if (v == null) {
            v = new ArrayList<Valintatulos>();
        }
        return v;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/hakukohde/{hakukohdeOid}")
    @PreAuthorize(READ_UPDATE_CRUD)
    @ApiOperation(value = "Valintatulosten haku hakukohteelle ja valintatapajonolle")
    public List<Valintatulos> hakukohteelle(
            @PathParam("hakukohdeOid") String hakukohdeOid) {
        List<Valintatulos> v = sijoitteluBusinessService.haeHakukohteenTilat(hakukohdeOid);
        if (v == null) {
            v = new ArrayList<Valintatulos>();
        }
        return v;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("haku/{hakuOid}/hakukohde/{hakukohdeOid}")
    @PreAuthorize(UPDATE_CRUD)
    @ApiOperation(value = "Valintatulosten tuonti hakukohteelle")
    public Response muutaHakemustenTilaa(@PathParam("hakuOid") String hakuOid,
                                         @PathParam("hakukohdeOid") String hakukohdeOid,
                                         List<Valintatulos> valintatulokset,
                                         @QueryParam("selite") String selite) {

        if(valintatulokset != null && !sijoitteluBusinessService.muutoksetOvatAjantasaisia(hakukohdeOid, valintatulokset)) {
            return Response.status(Status.CONFLICT).build();
        }

        try {
            Hakukohde hakukohde = sijoitteluBusinessService.getHakukohde(hakuOid, hakukohdeOid);
            for (Valintatulos v : valintatulokset) {
                sijoitteluBusinessService.vaihdaHakemuksenTila(hakuOid, hakukohde, v.getValintatapajonoOid(),
                        v.getHakemusOid(), v.getTila(), selite, v.getIlmoittautumisTila(), v.getJulkaistavissa(),
                        v.getHyvaksyttyVarasijalta(), v.getHyvaksyPeruuntunut());
                AUDIT.log(builder()
                        .id(username())
                        .hakuOid(hakuOid)
                        .hakukohdeOid(hakukohde.getOid())
                        .hakemusOid(v.getHakemusOid())
                        .valintatapajonoOid(v.getValintatapajonoOid())
                        .add("ilmoittautumistila", v.getIlmoittautumisTila())
                        .add("julkaistavissa", v.getJulkaistavissa())
                        .add("hyvaksyttyvarasijalta", v.getHyvaksyttyVarasijalta())
                        .add("hyvaksyperuuntunut", v.getHyvaksyPeruuntunut())
                        .add("selite", selite)
                        .add("valintatuloksentila", v.getTila())
                        .message("Hakemuksen tilan muuttaminen")
                        .build());
            }
            return Response.status(Status.OK).build();
        } catch (Exception e) {
            LOGGER.error("Valintatulosten tallenus epäonnistui haussa {} hakukohteelle {}", hakuOid, hakukohdeOid, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("erillishaku/haku/{hakuOid}/hakukohde/{hakukohdeOid}")
    @PreAuthorize(UPDATE_CRUD)
    @ApiOperation(value = "Valintatulosten tuonti erillishaun hakukohteelle")
    public Response muutaErillishaunHakemustenTilaa(@PathParam("hakuOid") String hakuOid,
                                         @PathParam("hakukohdeOid") String hakukohdeOid,
                                         List<Valintatulos> valintatulokset,
                                         @QueryParam("selite") String selite) {

        if(valintatulokset != null && !sijoitteluBusinessService.muutoksetOvatAjantasaisia(hakukohdeOid, valintatulokset)) {
            return Response.status(Response.Status.CONFLICT).build();
        }

        try {
            Hakukohde hakukohde = sijoitteluBusinessService.getErillishaunHakukohde(hakuOid, hakukohdeOid);
            for (Valintatulos v : valintatulokset) {
                ValintatuloksenTila tila = v.getTila();
                IlmoittautumisTila ilmoittautumisTila = v.getIlmoittautumisTila();
                sijoitteluBusinessService.vaihdaHakemuksenTila(hakuOid,
                        hakukohde, v.getValintatapajonoOid(),
                        v.getHakemusOid(), tila, selite, ilmoittautumisTila,
                        v.getJulkaistavissa(), v.getHyvaksyttyVarasijalta(), v.getHyvaksyPeruuntunut());
                AUDIT.log(builder()
                        .id(username())
                        .hakuOid(hakuOid)
                        .hakukohdeOid(hakukohde.getOid())
                        .hakemusOid(v.getHakemusOid())
                        .valintatapajonoOid(v.getValintatapajonoOid())
                        .add("ilmoittautumistila", v.getIlmoittautumisTila())
                        .add("julkaistavissa", v.getJulkaistavissa())
                        .add("hyvaksyttyvarasijalta", v.getHyvaksyttyVarasijalta())
                        .add("hyvaksyperuuntunut", v.getHyvaksyPeruuntunut())
                        .add("selite", selite)
                        .add("valintatuloksentila",v.getTila())
                        .message("Erillishaun hakemuksen tilan muuttaminen")
                        .build());
            }
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            LOGGER.error("Valintatulosten tallenus epäonnistui haussa " + hakuOid + " hakukohteelle " + hakukohdeOid, e);
            Map error = new HashMap();
            error.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/erillishaku/{hakuOid}/hakukohde/{hakukohdeOid}")
    @PreAuthorize(UPDATE_CRUD)
    @ApiOperation(value = "Erillishaun hakijoiden tuonti hakukohteelle")
    public Response tuoErillishaunHakijat(
            @ApiParam("valintatapajononNimi")
            @QueryParam("valintatapajononNimi") String valintatapajononNimi,
            @PathParam("hakuOid") String hakuOid,
            @PathParam("hakukohdeOid") String hakukohdeOid,
            List<ErillishaunHakijaDTO> erillishaunHakijaDtos) {
        if (erillishaunHakijaDtos == null || erillishaunHakijaDtos.isEmpty()) {
            LOGGER.error("Yritettiin tuoda tyhjaa joukkoa erillishaun hakijoiden tuontiin haussa {} hakukohteelle {}!", hakuOid, hakukohdeOid);
            throw new RuntimeException("Yritettiin tuoda tyhjaa joukkoa erillishaun hakijoiden tuontiin!");
        }
        try {
            LOGGER.info("Tuodaan erillishaun tietoja jonolle {}", erillishaunHakijaDtos.iterator().next().valintatapajonoOid);
            Map<Boolean, List<ErillishaunHakijaDTO>> ryhmitelty = erillishaunHakijaDtos.stream().collect(Collectors.partitioningBy(ErillishaunHakijaDTO::getPoistetaankoTulokset));

            ryhmitelty.getOrDefault(true, new ArrayList<>()).stream().forEach(
                    e -> {
                        if (e.getValintatapajonoOid() == null) {
                            throw new RuntimeException("Hakemuksen " + e.getHakemusOid() + " tuloksia ei voi poistaa, koska valintatapajonoOid on null");
                        } else {
                            poistaTulokset(e.getHakuOid(), e.getHakukohdeOid(), e.getHakemusOid(), e.getValintatapajonoOid());
                        }

                    }
            );

            ryhmitelty.getOrDefault(false, new ArrayList<>()).stream().forEach(
                    e -> muutaTilaa(
                            valintatapajononNimi,
                            e.tarjoajaOid, e.hakuOid,
                            e.hakukohdeOid, e.hakemusOid,
                            e.hakemuksenTila, Optional.empty(), Optional.ofNullable(e.valintatapajonoOid),
                            Optional.ofNullable(e.etunimi), Optional.ofNullable(e.sukunimi)));
            ryhmitelty.getOrDefault(false, new ArrayList<>())
                    .stream()
                    .map(e -> e.asValintatulos())
                    .forEach(v -> sijoitteluBusinessService.vaihdaHakemuksenTila(
                            v.getHakuOid(),
                            sijoitteluBusinessService.getHakukohde(v.getHakuOid(), v.getHakukohdeOid()),
                            v.getValintatapajonoOid(),
                            v.getHakemusOid(),
                            v.getTila(),
                            "Erillishauntuonti",
                            v.getIlmoittautumisTila(),
                            v.getJulkaistavissa(),
                            v.getHyvaksyttyVarasijalta(),
                            v.getHyvaksyPeruuntunut()));
            LOGGER.info("Erillishaun tietojen tuonti onnistui jonolle {} haussa {} hakukohteelle {}",
                    erillishaunHakijaDtos.iterator().next().valintatapajonoOid, hakuOid, hakukohdeOid);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            LOGGER.error("Error in erillishaunhakijat tuonti haussa " + hakuOid + " hakukohteelle " + hakukohdeOid, e);
            Map error = new HashMap();
            error.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    private void poistaTulokset(String hakuOid, String hakukohdeOid, String hakemusOid, String valintatapajonoOid) {
        Optional<SijoitteluAjo> sijoitteluAjoOpt = raportointiService.latestSijoitteluAjoForHaku(hakuOid);
        if (!sijoitteluAjoOpt.isPresent()) {
            throw new RuntimeException("Haulle " + hakuOid + " ei löytynyt sijoitteluajoja");
        } else {
            Optional<Hakukohde> hakukohde = Optional.ofNullable(hakukohdeDao.getHakukohdeForSijoitteluajo(sijoitteluAjoOpt.get().getSijoitteluajoId(), hakukohdeOid));
            if (!hakukohde.isPresent()) {
                throw new RuntimeException("hakukohteelle " + hakukohdeOid + " ei löytynyt tuloksia");
            } else {
                Optional<Hakemus> hakemus = hakukohde.get().getValintatapajonot()
                        .stream()
                        .filter(j -> j.getOid().equals(valintatapajonoOid))
                        .flatMap(j -> j.getHakemukset().stream())
                        .filter(h -> h.getHakemusOid().equals(hakemusOid))
                        .findFirst();

                if (!hakemus.isPresent()) {
                    throw new RuntimeException("hakukohteelle " + hakukohdeOid + " ei löytynyt tuloksia valintatapajonosta " + valintatapajonoOid + " hakemukselle " + hakemusOid);
                } else {
                    Valintatapajono valintatapajono = hakukohde.get().getValintatapajonot()
                            .stream()
                            .filter(j -> j.getOid().equals(valintatapajonoOid))
                            .findFirst()
                            .get();
                    valintatapajono.getHakemukset().remove(hakemus.get());
                    hakukohdeDao.persistHakukohde(hakukohde.get());

                    // Tarkistetaan vielä löytyykö valintatuloksia ja jos löytyy niin poistetaan ne
                    Valintatulos valintatulos = valintatulosDao.loadValintatulos(hakukohdeOid, valintatapajonoOid, hakemusOid);
                    if (valintatulos != null) {
                        valintatulosDao.remove(valintatulos);
                    }
                }
            }
        }
    }

    private void muutaTilaa(String valintatapajononNimi,
                            String tarjoajaOid, String hakuOid, String hakukohdeOid, String hakemusOid,
                            HakemuksenTila tila, Optional<List<String>> tilanKuvaukset,
                            Optional<String> valintatapajonoOid,
                            Optional<String> etunimi, Optional<String> sukunimi) {
        Sijoittelu sijoittelu = sijoitteluDao.getSijoitteluByHakuOid(hakuOid).orElseGet(() -> {
            Sijoittelu s = new Sijoittelu();
            s.setCreated(new Date());
            s.setSijoitteluId(System.currentTimeMillis());
            s.setHakuOid(hakuOid);
            SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
            sijoitteluAjo.setSijoitteluajoId(System.currentTimeMillis());
            sijoitteluAjo.setHakuOid(s.getHakuOid());
            s.getSijoitteluajot().add(sijoitteluAjo);
            return s;
        });
        SijoitteluAjo ajo = sijoittelu.getLatestSijoitteluajo();
        Hakukohde hakukohde = Optional.ofNullable(hakukohdeDao.getHakukohdeForSijoitteluajo(ajo.getSijoitteluajoId(), hakukohdeOid)).orElseGet(() -> {
            HakukohdeItem item = new HakukohdeItem();
            item.setOid(hakukohdeOid);
            ajo.getHakukohteet().add(item);

            Hakukohde h = new Hakukohde();
            h.setKaikkiJonotSijoiteltu(true);
            h.setOid(hakukohdeOid);
            h.setSijoitteluajoId(ajo.getSijoitteluajoId());
            h.setTarjoajaOid(tarjoajaOid);
            h.getValintatapajonot().add(createValintatapaJono(
                    valintatapajononNimi,
                    valintatapajonoOid.orElse(UUID.randomUUID().toString())));
            return h;
        });
        Valintatapajono jono = valintatapajonoOid.map(oid -> hakukohde.getValintatapajonot().stream()
                .filter(j -> oid.equals(j.getOid()))
                .findFirst()
                .orElseGet(() -> {
                    Valintatapajono j = createValintatapaJono(valintatapajononNimi, oid);
                    hakukohde.getValintatapajonot().add(j);
                    return j;
                }))
                .orElse(hakukohde.getValintatapajonot().get(0));
        Hakemus hakemus = jono.getHakemukset().stream()
                .filter(h -> hakemusOid.equals(h.getHakemusOid()))
                .findFirst()
                .orElseGet(() -> {
                    Hakemus h = new Hakemus();
                    h.setHakemusOid(hakemusOid);
                    h.setJonosija(1);
                    h.setPrioriteetti(1);
                    jono.getHakemukset().add(h);
                    return h;
                });

        if (StringUtils.isBlank(hakukohde.getTarjoajaOid())) {
            if (StringUtils.isBlank(tarjoajaOid)) {
                hakukohde.setTarjoajaOid(tarjontaIntegrationService.getTarjoajaOid(hakukohde.getOid())
                        .orElseThrow(() -> new RuntimeException("Hakukohteelle " + hakukohdeOid + " ei löytynyt tarjoajaOidia")));
            } else {
                hakukohde.setTarjoajaOid(tarjoajaOid);
            }
        }
        hakemus.setTila(tila);
        tilanKuvaukset.ifPresent(kuvaukset -> {
            if (kuvaukset.size() == 3) {
                hakemus.getTilanKuvaukset().put("FI", kuvaukset.get(0));
                hakemus.getTilanKuvaukset().put("SV", kuvaukset.get(1));
                hakemus.getTilanKuvaukset().put("EN", kuvaukset.get(2));
            }
        });
        etunimi.ifPresent(hakemus::setEtunimi);
        sukunimi.ifPresent(hakemus::setSukunimi);

        jono.setHyvaksytty(getMaara(jono.getHakemukset(), Arrays.asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY)));
        jono.setVaralla(getMaara(jono.getHakemukset(), Arrays.asList(HakemuksenTila.VARALLA)));

        hakukohdeDao.persistHakukohde(hakukohde);
        sijoitteluDao.persistSijoittelu(sijoittelu);
    }

    private Valintatapajono createValintatapaJono(String valintatapajononNimi, String valintatapajonoOid) {
        Valintatapajono jono = new Valintatapajono();
        jono.setHyvaksytty(0);
        jono.setVaralla(0);
        jono.setOid(valintatapajonoOid);
        jono.setAloituspaikat(0);
        jono.setPrioriteetti(0);
        jono.setNimi(valintatapajononNimi);

        return jono;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("haku/{hakuOid}/hakukohde/{hakukohdeOid}/hakemus/{hakemusOid}")
    @PreAuthorize(UPDATE_CRUD)
    @ApiOperation(value = "Hakemuksen sijoittelun tilan muuttaminen")
    public Response muutaSijoittelunTilaa(
            @QueryParam("valintatapajononNimi") String valintatapajononNimi,
            @PathParam("hakuOid") String hakuOid,
            @PathParam("hakukohdeOid") String hakukohdeOid,
            @PathParam("hakemusOid") String hakemusOid, Tila tilaObj,
            @QueryParam("tarjoajaOid") String tarjoajaOid) {

        try {
            HakemuksenTila tila;

            if (StringUtils.isNotBlank(tilaObj.getTila())) {
                tila = HakemuksenTila.valueOf(tilaObj.getTila());
            } else {
                if (tilaObj.isHyvaksy()) {
                    tila = HakemuksenTila.HYVAKSYTTY;
                } else {
                    tila = HakemuksenTila.HYLATTY;
                }
            }
            Optional<List<String>> kuvaukset = Optional.ofNullable(tilaObj.getTilanKuvaukset());
            muutaTilaa(valintatapajononNimi, tarjoajaOid, hakuOid, hakukohdeOid, hakemusOid, tila, kuvaukset,
                    Optional.empty(), Optional.empty(), Optional.empty());
            return Response.status(Response.Status.OK).build();

        } catch (Exception e) {
            LOGGER.error("Hakemuksen tilan asetus epäonnistui haussa {} hakukohteelle {} ja hakemukselle {}", hakuOid, hakukohdeOid, hakemusOid, e);
            Map error = new HashMap();
            error.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error).build();
        }
    }

    private int getMaara(List<Hakemus> hakemukset, List<HakemuksenTila> tilat) {
        return (int) hakemukset.parallelStream().filter(h -> tilat.indexOf(h.getTila()) != -1)
                .reduce(0, (sum, b) -> sum + 1, Integer::sum);
    }
}
