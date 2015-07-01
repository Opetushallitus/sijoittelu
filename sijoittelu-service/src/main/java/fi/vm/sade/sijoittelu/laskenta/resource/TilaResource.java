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

        if(valintatulokset != null && !sijoitteluBusinessService.muutoksetOvatAjantasaisia(valintatulokset)) {
            return Response.status(Response.Status.CONFLICT).build();
        }

        try {
            Hakukohde hakukohde = sijoitteluBusinessService.getHakukohde(hakuOid, hakukohdeOid);
            for (Valintatulos v : valintatulokset) {
                ValintatuloksenTila tila = v.getTila();
                IlmoittautumisTila ilmoittautumisTila = v.getIlmoittautumisTila();
                sijoitteluBusinessService.vaihdaHakemuksenTila(hakuOid,
                        hakukohde, v.getValintatapajonoOid(),
                        v.getHakemusOid(), tila, selite, ilmoittautumisTila,
                        v.getJulkaistavissa(), v.getHyvaksyttyVarasijalta());
            }
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            LOGGER.error("Valintatulosten tallenus epäonnistui haussa {} hakukohteelle {}. {}\r\n{}",
                    hakuOid, hakukohdeOid, e.getMessage(), Arrays.toString(e.getStackTrace()));
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
                    .forEach(
                            v -> {
                                ValintatuloksenTila tila = v.getTila();
                                IlmoittautumisTila ilmoittautumisTila = v
                                        .getIlmoittautumisTila();
                                sijoitteluBusinessService.vaihdaHakemuksenTila(
                                        v.getHakuOid(),
                                        sijoitteluBusinessService.getHakukohde(v.getHakuOid(), v.getHakukohdeOid()),
                                        v.getValintatapajonoOid(),
                                        v.getHakemusOid(), tila,
                                        "Erillishauntuonti",
                                        ilmoittautumisTila,
                                        v.getJulkaistavissa(),
                                        v.getHyvaksyttyVarasijalta());
                            });
            LOGGER.info("Erillishaun tietojen tuonti onnistui jonolle {} haussa {} hakukohteelle {}",
                    erillishaunHakijaDtos.iterator().next().valintatapajonoOid, hakuOid, hakukohdeOid);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            LOGGER.error("Error in erillishaunhakijat tuonti haussa {} hakukohteelle {}! {}\r\n{}",
                    e.getMessage(), Arrays.toString(e.getStackTrace()), hakuOid, hakukohdeOid);
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

    private void muutaTilaa(String valintatapajononNimi, String tarjoajaOid, String hakuOid, String hakukohdeOid, String hakemusOid, HakemuksenTila tila,
            Optional<List<String>> tilanKuvaukset, Optional<String> valintatapajonoOid, Optional<String> etunimi, Optional<String> sukunimi) {
        Optional<SijoitteluAjo> sijoitteluAjoOpt = raportointiService.latestSijoitteluAjoForHaku(hakuOid);

        if (!sijoitteluAjoOpt.isPresent()) {
            Sijoittelu sijoittelu = new Sijoittelu();
            sijoittelu.setCreated(new Date());
            sijoittelu.setSijoitteluId(System.currentTimeMillis());
            sijoittelu.setHakuOid(hakuOid);

            SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
            Long now = System.currentTimeMillis();
            sijoitteluAjo.setSijoitteluajoId(now);
            sijoitteluAjo.setHakuOid(sijoittelu.getHakuOid());
            sijoittelu.getSijoitteluajot().add(sijoitteluAjo);

            sijoitteluDao.persistSijoittelu(sijoittelu);

            sijoitteluAjoOpt = Optional.of(sijoitteluAjo);
        }

        SijoitteluAjo ajo = sijoitteluAjoOpt.get();

        Optional<HakukohdeItem> itemOpt = ajo.getHakukohteet()
                .parallelStream()
                .filter(h -> h.getOid().equals(hakukohdeOid)).findFirst();
        if (!itemOpt.isPresent()) {
            Sijoittelu sijoittelu = sijoitteluDao.getSijoitteluByHakuOid(hakuOid).get();
            HakukohdeItem item = new HakukohdeItem();
            item.setOid(hakukohdeOid);
            sijoittelu.getLatestSijoitteluajo().getHakukohteet().add(item);

            sijoitteluDao.persistSijoittelu(sijoittelu);

            Hakukohde hakukohde = new Hakukohde();
            hakukohde.setKaikkiJonotSijoiteltu(true);
            hakukohde.setOid(hakukohdeOid);
            hakukohde.setSijoitteluajoId(ajo.getSijoitteluajoId());
            hakukohde.setTarjoajaOid(tarjoajaOid);

            Valintatapajono jono;
            if (valintatapajonoOid.isPresent()) {
                jono = createValintatapaJono(valintatapajononNimi, valintatapajonoOid.get());
            } else {
                jono = createValintatapaJono(valintatapajononNimi, UUID.randomUUID().toString());
            }
            hakukohde.getValintatapajonot().add(jono);
            hakukohdeDao.persistHakukohde(hakukohde);
        }

        Hakukohde kohde = hakukohdeDao.getHakukohdeForSijoitteluajo(
                ajo.getSijoitteluajoId(), hakukohdeOid);
        if (kohde != null) {
            if (kohde.getTarjoajaOid() == null || StringUtils.isBlank(kohde.getTarjoajaOid())) {
                if (tarjoajaOid == null || StringUtils.isBlank(tarjoajaOid)) {
                    try {
                        Optional<String> tOid = tarjontaIntegrationService.getTarjoajaOid(kohde.getOid());
                        if (tOid.isPresent()) {
                            kohde.setTarjoajaOid(tOid.get());
                            hakukohdeDao.persistHakukohde(kohde);
                        } else {
                            throw new RuntimeException("Hakukohteelle " + hakukohdeOid + " ei löytynyt tarjoajaOidia sijoitteluajosta");
                        }
                    } catch (Exception e) {
                        LOGGER.error("Tilan muuttamisessa virhetilanne!", e);
                        throw new RuntimeException("Hakukohteelle " + hakukohdeOid + " ei löytynyt tarjoajaOidia sijoitteluajosta");
                    }
                } else {
                    kohde.setTarjoajaOid(tarjoajaOid);
                    hakukohdeDao.persistHakukohde(kohde);
                }
            }

            Valintatapajono jono;
            if (valintatapajonoOid.isPresent()) {
                Optional<Valintatapajono> valintatapajonoOptional = kohde.getValintatapajonot()
                        .stream()
                        .filter(j -> j.getOid().equals(valintatapajonoOid.get()))
                        .findFirst();
                if (valintatapajonoOptional.isPresent()) {
                    jono = valintatapajonoOptional.get();
                } else {
                    jono = createValintatapaJono(valintatapajononNimi, valintatapajonoOid.get());
                    kohde.getValintatapajonot().add(jono);
                }

            } else {
                jono = kohde.getValintatapajonot().get(0);
            }

            Optional<Hakemus> hakemusOpt = jono.getHakemukset()
                    .parallelStream()
                    .filter(h -> h.getHakemusOid().equals(hakemusOid))
                    .findFirst();

            if (hakemusOpt.isPresent()) {
                hakemusOpt.get().setTila(tila);

                if (tilanKuvaukset.isPresent() && tilanKuvaukset.get().size() == 3) {
                    hakemusOpt.get().getTilanKuvaukset().put("FI", tilanKuvaukset.get().get(0));
                    hakemusOpt.get().getTilanKuvaukset().put("SV", tilanKuvaukset.get().get(1));
                    hakemusOpt.get().getTilanKuvaukset().put("EN", tilanKuvaukset.get().get(2));
                }
                if (etunimi.isPresent()) {
                    hakemusOpt.get().setEtunimi(etunimi.get());
                }
                if (sukunimi.isPresent()) {
                    hakemusOpt.get().setSukunimi(sukunimi.get());
                }

            } else {
                Hakemus hakemus = new Hakemus();
                hakemus.setHakemusOid(hakemusOid);
                hakemus.setJonosija(1);
                hakemus.setPrioriteetti(1);
                hakemus.setTila(tila);
                if (tilanKuvaukset.isPresent() && tilanKuvaukset.get().size() == 3) {
                    hakemus.getTilanKuvaukset().put("FI", tilanKuvaukset.get().get(0));
                    hakemus.getTilanKuvaukset().put("SV", tilanKuvaukset.get().get(1));
                    hakemus.getTilanKuvaukset().put("EN", tilanKuvaukset.get().get(2));
                }
                if (etunimi.isPresent()) {
                    hakemus.setEtunimi(etunimi.get());
                }
                if (sukunimi.isPresent()) {
                    hakemus.setSukunimi(sukunimi.get());
                }
                jono.getHakemukset().add(hakemus);
            }
            jono.setHyvaksytty(getMaara(jono.getHakemukset(), Arrays.asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY)));
            jono.setVaralla(getMaara(jono.getHakemukset(), Arrays.asList(HakemuksenTila.VARALLA)));

            hakukohdeDao.persistHakukohde(kohde);
        }
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
            return Response.status(Response.Status.ACCEPTED).build();

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
