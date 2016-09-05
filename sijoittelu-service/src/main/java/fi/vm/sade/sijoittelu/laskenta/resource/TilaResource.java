package fi.vm.sade.sijoittelu.laskenta.resource;

import static fi.vm.sade.sijoittelu.laskenta.roles.SijoitteluRole.READ_UPDATE_CRUD;
import static fi.vm.sade.sijoittelu.laskenta.roles.SijoitteluRole.UPDATE_CRUD;
import static fi.vm.sade.sijoittelu.laskenta.util.SijoitteluAudit.username;
import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.CRUD;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.dto.ErillishaunHakijaDTO;
import fi.vm.sade.sijoittelu.laskenta.service.business.IllegalVTSRequestException;
import fi.vm.sade.sijoittelu.laskenta.service.business.PriorAcceptanceException;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.StaleReadException;
import fi.vm.sade.sijoittelu.laskenta.service.exception.HakemustaEiLoytynytException;
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
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Path("tila")
@PreAuthorize("isAuthenticated()")
@Api(value = "tila", description = "Resurssi sijoittelun tilojen kasittelyyn")
public class TilaResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(TilaResource.class);

    static final String LATEST = "latest";

    @Autowired
    public SijoitteluBusinessService sijoitteluBusinessService;

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
    @Path("/{hakemusOid}")
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
    @Path("/{hakemusOid}/{hakuOid}/{hakukohdeOid}/{valintatapajonoOid}/")
    @PreAuthorize(READ_UPDATE_CRUD)
    @ApiOperation(value = "Hakemuksen valintatulosten haku tietyssa hakukohteessa ja valintatapajonossa")
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
    @Path("/haku/{hakuOid}/hakukohde/{hakukohdeOid}")
    @PreAuthorize(UPDATE_CRUD)
    @ApiOperation(value = "Valintatulosten tuonti hakukohteelle")
    public Response muutaHakemustenTilaa(@PathParam("hakuOid") String hakuOid,
                                         @PathParam("hakukohdeOid") String hakukohdeOid,
                                         List<Valintatulos> valintatulokset,
                                         @QueryParam("selite") String selite) {
        List<ValintatulosUpdateStatus> statuses = new ArrayList<>();
        try {
            Hakukohde hakukohde = sijoitteluBusinessService.getHakukohde(hakuOid, hakukohdeOid);
            processVaihdaHakemuksienTilat(statuses,valintatulokset,hakuOid,hakukohdeOid,hakukohde,selite);
            Status s = statuses.isEmpty() ? Status.OK : Status.INTERNAL_SERVER_ERROR;
            return Response.status(s)
                    .entity(new HakukohteenValintatulosUpdateStatuses(statuses))
                    .build();
        } catch (Exception e) {
            LOGGER.error("haku: {}, hakukohde: {}", hakuOid, hakukohdeOid, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new HakukohteenValintatulosUpdateStatuses(e.getMessage(), statuses))
                    .build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/haku/{hakuOid}/hakukohde/{hakukohdeOid}/valintatapajono/{valintatapajonoOid}/valintaesitys")
    @PreAuthorize(UPDATE_CRUD)
    @ApiOperation(value = "Valintaesityksen merkkaaminen hyvaksytyksi")
    public Response merkkaaJononValintaesitysHyvaksytyksi(@PathParam("hakuOid") String hakuOid,
                                                    @PathParam("hakukohdeOid") String hakukohdeOid,
                                                    @PathParam("valintatapajonoOid") String valintatapajonoOid,
                                                    List<Valintatulos> valintatulokset,
                                                    @QueryParam("selite") String selite,
                                                    @QueryParam("hyvaksytty") Boolean hyvaksytty) {
        try {
            List<ValintatulosUpdateStatus> statuses = new ArrayList<>();
            Hakukohde hakukohde = sijoitteluBusinessService.getHakukohde(hakuOid, hakukohdeOid);
            processVaihdaHakemuksienTilat(statuses, valintatulokset, hakuOid, hakukohdeOid, hakukohde, selite);
            if (!statuses.isEmpty()) {
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity(new HakukohteenValintatulosUpdateStatuses(statuses))
                        .build();
            }
            sijoitteluBusinessService.asetaJononValintaesitysHyvaksytyksi(hakukohde, valintatapajonoOid, hyvaksytty, hakuOid);
            return Response.status(Response.Status.OK)
                    .entity(new HakukohteenValintatulosUpdateStatuses(statuses))
                    .build();
        } catch (Exception e) {
            LOGGER.error("valintaesityksen hyväksymismerkintä(={}) epäonnistui. haku: {}, hakukohde: {}, valintatapajono: {valintatapajonoOid}", hyvaksytty, hakuOid, hakukohdeOid, valintatapajonoOid, e);
            Map<String,String> error = new HashMap<>();
            error.put("message", e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/checkStaleRead")
    @PreAuthorize(UPDATE_CRUD)
    @ApiOperation(value = "Tarkista onko Valintatuloksia muutettu hakemisen jalkeen. Palauttaa aina onnistuessaan 200 OK ja \"stale read\" -virheet vain rivikohtaisesti vastauksessa.")
    public Response tarkistaEtteiMuutoksiaHakemisenJalkeen(List<Valintatulos> valintatulokset) {
        List<ValintatulosUpdateStatus> statuses = new ArrayList<>();
        try {
            valintatulokset.stream().forEach(valintatulos -> {
                Valintatulos fromDb = valintatulosDao.loadValintatulos(
                    valintatulos.getHakukohdeOid(),
                    valintatulos.getValintatapajonoOid(),
                    valintatulos.getHakemusOid()
                );
                Date latestChangeInDB = fromDb == null ? null : fromDb.getViimeinenMuutos();
                boolean fresh = latestChangeInDB == null ||
                    latestChangeInDB.before(valintatulos.getRead());
                if (!fresh) {
                    statuses.add(new ValintatulosUpdateStatus(Status.CONFLICT.getStatusCode(), "Yritettiin muokata muuttunutta valintatulosta", fromDb.getValintatapajonoOid(), fromDb.getHakemusOid()));
                    long disparityMillis = latestChangeInDB.getTime() - valintatulos.getRead().getTime();
                    LOGGER.warn("Stale read: latest change from db {}, change read at {}, difference {} ms - of {}, when checking {}",
                        latestChangeInDB, valintatulos.getRead(), disparityMillis, fromDb, valintatulos);
                }
            });
            return Response.status(Status.OK)
                .entity(new HakukohteenValintatulosUpdateStatuses(statuses))
                .build();
        } catch (Exception e) {
            LOGGER.error(String.format("Poikkeus tarkistettaessa %d valintatuloksen tuoreutta", valintatulokset.size()), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new HakukohteenValintatulosUpdateStatuses(e.getMessage(), statuses))
                    .build();
        }
    }

    private void processVaihdaHakemuksienTilat(List<ValintatulosUpdateStatus> statuses, List<Valintatulos> valintatulokset, String hakuOid, String hakukohdeOid, Hakukohde hakukohde,  String selite) {
        for (Valintatulos v : valintatulokset) {
            try {
                sijoitteluBusinessService.vaihdaHakemuksenTila(hakuOid, hakukohde, v, selite, username());
            } catch (HakemustaEiLoytynytException e) {
                LOGGER.info("haku: {}, hakukohde: {}", hakuOid, hakukohdeOid, e);
                statuses.add(new ValintatulosUpdateStatus(Status.NOT_FOUND.getStatusCode(), e.getMessage(), v.getValintatapajonoOid(), v.getHakemusOid()));
            } catch (NotAuthorizedException e) {
                LOGGER.info("haku: {}, hakukohde: {}, valintatapajono: {}, hakemus: {}",
                        hakuOid, hakukohdeOid, v.getValintatapajonoOid(), v.getHakemusOid(), e);
                statuses.add(new ValintatulosUpdateStatus(Status.UNAUTHORIZED.getStatusCode(), e.getMessage(), v.getValintatapajonoOid(), v.getHakemusOid()));
            } catch (PriorAcceptanceException e) {
                LOGGER.info("haku: {}, hakukohde: {}, valintatapajono: {}, hakemus: {}",
                        hakuOid, hakukohdeOid, v.getValintatapajonoOid(), v.getHakemusOid(), e);
                statuses.add(new ValintatulosUpdateStatus(Status.FORBIDDEN.getStatusCode(), e.getMessage(), v.getValintatapajonoOid(), v.getHakemusOid()));
            } catch (IllegalArgumentException | IllegalVTSRequestException e) {
                LOGGER.info("haku: {}, hakukohde: {}, valintatapajono: {}, hakemus: {}",
                        hakuOid, hakukohdeOid, v.getValintatapajonoOid(), v.getHakemusOid(), e);
                statuses.add(new ValintatulosUpdateStatus(Status.BAD_REQUEST.getStatusCode(), e.getMessage(), v.getValintatapajonoOid(), v.getHakemusOid()));
            } catch (StaleReadException e) {
                LOGGER.info("", e);
                statuses.add(new ValintatulosUpdateStatus(Status.CONFLICT.getStatusCode(), e.getMessage(), v.getValintatapajonoOid(), v.getHakemusOid()));
            } catch (Exception e) {
                LOGGER.error("haku: {}, hakukohde: {}, valintatapajono: {}, hakemus: {}",
                        hakuOid, hakukohdeOid, v.getValintatapajonoOid(), v.getHakemusOid(), e);
                statuses.add(new ValintatulosUpdateStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage(), v.getValintatapajonoOid(), v.getHakemusOid()));
            }
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
        List<ValintatulosUpdateStatus> statuses = new ArrayList<>();
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
                            Optional.ofNullable(e.etunimi), Optional.ofNullable(e.sukunimi),
                            Optional.ofNullable(e.getValintatuloksenTila())));


            ryhmitelty.getOrDefault(false, new ArrayList<>())
                    .stream()
                    .map(e -> e.asValintatulos())
                    .collect(Collectors.groupingBy(v -> v.getHakukohdeOid()))
                    .forEach((k, v) -> processVaihdaHakemuksienTilat(statuses, v, hakuOid, k, sijoitteluBusinessService.getHakukohde(hakuOid, k), "Erillishauntuonti"));

            LOGGER.info("Erillishaun tietojen tuonti onnistui jonolle {} haussa {} hakukohteelle {}. Erroreita: {}",
                    erillishaunHakijaDtos.iterator().next().valintatapajonoOid, hakuOid, hakukohdeOid, !statuses.isEmpty());
            Status s = statuses.isEmpty() ? Status.OK : Status.INTERNAL_SERVER_ERROR;
            return Response.status(s)
                    .entity(new HakukohteenValintatulosUpdateStatuses(statuses))
                    .build();
       } catch (Exception e) {
            LOGGER.error("Error in erillishaunhakijat tuonti haussa " + hakuOid + " hakukohteelle " + hakukohdeOid, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new HakukohteenValintatulosUpdateStatuses(e.getMessage(), statuses))
                    .build();
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
                    hakukohdeDao.persistHakukohde(hakukohde.get(), hakuOid);

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
                            Optional<String> etunimi, Optional<String> sukunimi,
                            Optional<ValintatuloksenTila> valintatuloksenTila) {
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
        hakemus.getTilaHistoria().add(new TilaHistoria(tila));
        if (tilanKuvaukset.isPresent()) {
            if (tilanKuvaukset.get().size() == 3) {
                hakemus.getTilanKuvaukset().put("FI", tilanKuvaukset.get().get(0));
                hakemus.getTilanKuvaukset().put("SV", tilanKuvaukset.get().get(1));
                hakemus.getTilanKuvaukset().put("EN", tilanKuvaukset.get().get(2));
            }
        } else if (valintatuloksenTila.isPresent() && valintatuloksenTila.get().equals(ValintatuloksenTila.OTTANUT_VASTAAN_TOISEN_PAIKAN)) {
            hakemus.setTilanKuvaukset(TilanKuvaukset.peruuntunutYlempiToive());
        } else {
            hakemus.setTilanKuvaukset(TilanKuvaukset.tyhja);
        }

        etunimi.ifPresent(hakemus::setEtunimi);
        sukunimi.ifPresent(hakemus::setSukunimi);

        jono.setHyvaksytty(getMaara(jono.getHakemukset(), Arrays.asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY)));
        jono.setVaralla(getMaara(jono.getHakemukset(), Arrays.asList(HakemuksenTila.VARALLA)));

        hakukohdeDao.persistHakukohde(hakukohde, hakuOid);
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
    @Path("/haku/{hakuOid}/hakukohde/{hakukohdeOid}/hakemus/{hakemusOid}")
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
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            return Response.status(Response.Status.OK).build();

        } catch (Exception e) {
            LOGGER.error("Hakemuksen tilan asetus epäonnistui haussa {} hakukohteelle {} ja hakemukselle {}", hakuOid, hakukohdeOid, hakemusOid, e);
            Map error = new HashMap();
            error.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error).build();
        }
    }

    @PreAuthorize(CRUD)
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Kaynnistaa vanhojen sijoitteluajojen siivoamisen annetuilta hauilta")
    @Path("/siivoaVanhatSijoitteluAjot")
    public List<String> siivoaVanhatSijoitteluajot(List<String> hakuOids,
                                                   @ApiParam(value = "Montako ajoa saastetaan") @QueryParam("saastettavienAjojenMaara") Integer saastettavienAjojenMaaraParam) {
        List<String> resultMessages = new ArrayList<>();
        int ajojaSaastetaan = saastettavienAjojenMaaraParam == null ? sijoitteluBusinessService.getMaxAjoMaara() : saastettavienAjojenMaaraParam;
        LOGGER.info(String.format("Käynnistetään %s annetun haun vanhojen sijoitteluajojen siivous. Säästetään korkeintaan %s ajoa.", hakuOids.size(), ajojaSaastetaan));
        LOGGER.info("Hakujen oidit: " + hakuOids);
        for (String hakuOid : hakuOids) {
            Optional<Sijoittelu> sijoittelu = sijoitteluDao.getSijoitteluByHakuOid(hakuOid);
            if (sijoittelu.isPresent()) {
                sijoitteluBusinessService.siivoaVanhatAjotSijoittelulta(hakuOid, sijoittelu.get(), ajojaSaastetaan);
                resultMessages.add("Lähetettiin siivouspyyntö haun " + hakuOid + " sijoittelulle " + sijoittelu.get().getSijoitteluId());
            } else {
                String msg = "Ei löytynyt sijoittelua siivottavaksi haulle " + hakuOid;
                LOGGER.warn(msg);
                resultMessages.add(msg);
            }
        }
        return resultMessages;
    }

    private int getMaara(List<Hakemus> hakemukset, List<HakemuksenTila> tilat) {
        return (int) hakemukset.parallelStream().filter(h -> tilat.indexOf(h.getTila()) != -1)
                .reduce(0, (sum, b) -> sum + 1, Integer::sum);
    }
}
