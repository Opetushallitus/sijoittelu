package fi.vm.sade.sijoittelu.jatkuva.resource;

import com.google.gson.Gson;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.Changes;
import fi.vm.sade.sijoittelu.jatkuva.dao.JatkuvaSijoitteluDAO;
import fi.vm.sade.sijoittelu.jatkuva.dto.JatkuvaSijoittelu;
import fi.vm.sade.sijoittelu.jatkuva.dto.LuoJatkuvaSijoittelu;
import fi.vm.sade.sijoittelu.jatkuva.external.resource.tarjonta.TarjontaAsyncResource;
import fi.vm.sade.sijoittelu.jatkuva.dto.AjastettuSijoitteluInfo;
import fi.vm.sade.sijoittelu.jatkuva.dto.Sijoittelu;
import fi.vm.sade.sijoittelu.jatkuva.service.JatkuvaSijoitteluService;
import fi.vm.sade.sijoittelu.jatkuva.util.SecurityUtil;
import fi.vm.sade.sijoittelu.jatkuva.parametrit.service.HakuParametritService;
import fi.vm.sade.sijoittelu.jatkuva.security.AuthorityCheckService;
import fi.vm.sade.sijoittelu.jatkuva.service.SijoittelunAktivointiService;
import fi.vm.sade.sijoittelu.jatkuva.service.SijoittelunValvontaService;
import fi.vm.sade.valinta.sharedutils.AuditLog;
import fi.vm.sade.valinta.sharedutils.ValintaResource;
import fi.vm.sade.valinta.sharedutils.ValintaperusteetOperation;
import io.reactivex.Observable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@RestController("SijoitteluAktivointiResource")
@RequestMapping("/resources/koostesijoittelu")
@PreAuthorize("isAuthenticated()")
@Tag(
    name = "/koostesijoittelu",
    description = "Ohjausparametrit palveluiden aktiviteettipäivämäärille")
@Profile({"default", "dev"})
public class SijoitteluAktivointiResource {
  private static final Logger LOG = LoggerFactory.getLogger(SijoitteluAktivointiResource.class);
  public static final String OPH_CRUD =
      "hasAnyRole('ROLE_APP_SIJOITTELU_CRUD_1.2.246.562.10.00000000001')";
  public static final String ANY_CRUD = "hasAnyRole('ROLE_APP_SIJOITTELU_CRUD')";
  public static final String OPH_CRUD_ROLE = "ROLE_APP_SIJOITTELU_CRUD_1.2.246.562.10.00000000001";

  @Autowired private SijoittelunAktivointiService sijoitteluAktivointiProxy;

  @Autowired(required = false)
  private JatkuvaSijoitteluService jatkuvaSijoittelu;

  @Autowired private HakuParametritService hakuParametritService;

  @Autowired private JatkuvaSijoitteluDAO sijoittelunSeurantaResource;

  @Autowired private SijoittelunValvontaService sijoittelunValvonta;

  @Autowired private TarjontaAsyncResource tarjontaResource;

  @Autowired private AuthorityCheckService authorityCheckService;

  @Autowired private Audit audit;

  @GetMapping(value = "/status/{hakuoid:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Sijoittelun status",
      responses = {
        @ApiResponse(
            responseCode = "OK",
            content = @Content(schema = @Schema(implementation = Sijoittelu.class)))
      })
  public Sijoittelu status(@PathVariable("hakuoid") String hakuOid) {
    return sijoittelunValvonta.haeAktiivinenSijoitteluHaulle(hakuOid);
  }

  @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Jatkuvan sijoittelun jonossa olevat sijoittelut",
      responses = {
        @ApiResponse(
            responseCode = "OK",
            content = @Content(schema = @Schema(implementation = String.class)))
      })
  public List<AjastettuSijoitteluInfo> status() {
    return jatkuvaSijoittelu.haeAjossaOlevatAjastetutSijoittelut();
  }

  @PostMapping(value = "/aktivoi")
  @PreAuthorize("hasAnyRole('ROLE_APP_VALINTOJENTOTEUTTAMINEN_CRUD')")
  @Operation(
      summary = "Sijoittelun aktivointi",
      responses = {
        @ApiResponse(
            responseCode = "OK",
            content = @Content(schema = @Schema(implementation = Void.class)))
      })
  public void aktivoiSijoittelu(
      @RequestParam(value = "hakuOid", required = false) String hakuOid,
      HttpServletRequest request) {
    authorityCheckService.checkAuthorizationForHaku(
        hakuOid, Collections.singleton("ROLE_APP_SIJOITTELU_CRUD"));

    if (!hakuParametritService.getParametritForHaku(hakuOid).valinnanhallintaEnabled()) {
      LOG.error("Sijoittelua yritettiin käynnistää haulle({}) ilman käyttöoikeuksia!", hakuOid);
      throw new RuntimeException("Ei käyttöoikeuksia!");
    }

    if (StringUtils.isBlank(hakuOid)) {
      LOG.error("Sijoittelua yritettiin käynnistää ilman hakuOidia!");
      throw new RuntimeException("Parametri hakuOid on pakollinen!");
    } else {
      AuditLog.log(
          audit,
          AuditLog.getUser(request),
          ValintaperusteetOperation.SIJOITTELU_KAYNNISTYS,
          ValintaResource.SIJOITTELUAKTIVOINTI,
          hakuOid,
          Changes.EMPTY);
      sijoitteluAktivointiProxy.aktivoiSijoittelu(new Sijoittelu(hakuOid));
    }
  }

  @Deprecated //"Valintalaskenta-ui:n käyttämä rajapinta. Käytä Post /jatkuva endpointtia luodaksesi aktiivisen ajastetun sijoittelun"
  @GetMapping(value = "/jatkuva/aktivoi", produces = MediaType.TEXT_PLAIN_VALUE)
  @PreAuthorize(ANY_CRUD)
  @Operation(
      summary = "Ajastetun sijoittelun aktivointi",
      responses = {
        @ApiResponse(
            responseCode = "OK",
            content = @Content(schema = @Schema(implementation = String.class)))
      })
  public String aktivoiJatkuvassaSijoittelussa(
      @RequestParam(value = "hakuOid", required = false) String hakuOid) {
    if (!hakuParametritService.getParametritForHaku(hakuOid).valinnanhallintaEnabled()) {
      return "no privileges.";
    }

    if (StringUtils.isBlank(hakuOid)) {
      return "get parameter 'hakuOid' required";
    } else {
      JatkuvaSijoittelu jatkuvaSijoittelu = sijoittelunSeurantaResource.hae(hakuOid);
      if (jatkuvaSijoittelu.getAloitusajankohta() == null || jatkuvaSijoittelu.getAjotiheys() == null) {
        LOG.warn(
            "Haulta {} puuttuu jatkuvan sijoittelun parametreja. Ei aktivoida jatkuvaa sijoittelua.");
        return "ei aktivoitu";
      } else {
        authorityCheckService.checkAuthorizationForHaku(
            hakuOid, Collections.singleton("ROLE_APP_SIJOITTELU_CRUD"));

        LOG.info("jatkuva sijoittelu aktivoitu haulle {}", hakuOid);
        sijoittelunSeurantaResource.merkkaaSijoittelunAjossaTila(hakuOid, true);
        return "aktivoitu";
      }
    }
  }

  @GetMapping(value = "/jatkuva/poista", produces = MediaType.TEXT_PLAIN_VALUE)
  @PreAuthorize(ANY_CRUD)
  @Operation(
      summary = "Ajastetun sijoittelun deaktivointi",
      responses = {
        @ApiResponse(
            responseCode = "OK",
            content = @Content(schema = @Schema(implementation = String.class)))
      })
  public ResponseEntity<String> poistaJatkuvastaSijoittelusta(
      @RequestParam(value = "hakuOid", required = false) String hakuOid) {
    if (!hakuParametritService.getParametritForHaku(hakuOid).valinnanhallintaEnabled()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("no privileges.");
    }

    if (StringUtils.isBlank(hakuOid)) {
      return ResponseEntity.badRequest().body("get parameter 'hakuOid' required");
    } else {
      authorityCheckService.checkAuthorizationForHaku(
          hakuOid, Collections.singleton("ROLE_APP_SIJOITTELU_CRUD"));

      LOG.info("jatkuva sijoittelu poistettu haulta {}", hakuOid);
      sijoittelunSeurantaResource.poistaSijoittelu(hakuOid);
      return ResponseEntity.ok("poistettu");
    }
  }

  @GetMapping(value = "/jatkuva/kaikki", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(OPH_CRUD)
  @Operation(
      summary = "Kaikki aktiiviset sijoittelut",
      responses = {
        @ApiResponse(
            responseCode = "OK",
            content = @Content(schema = @Schema(implementation = List.class)))
      })
  public Collection<JatkuvaSijoittelu> aktiivisetSijoittelut() {
    return sijoittelunSeurantaResource.hae();
  }

  @GetMapping(value = "/jatkuva", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(ANY_CRUD)
  @Operation(
      summary = "Haun aktiiviset sijoittelut",
      responses = {
        @ApiResponse(
            responseCode = "OK",
            content = @Content(schema = @Schema(implementation = String.class)))
      })
  public DeferredResult<ResponseEntity<String>> jatkuvaTila(
      @RequestParam(value = "hakuOid", required = false) String hakuOid) {

    DeferredResult<ResponseEntity<String>> result = new DeferredResult<>(2 * 60 * 1000l);
    result.onTimeout(
        () -> {
          LOG.error(
              "Haun aktiiviset sijoittelut -palvelukutsu on aikakatkaistu: /koostesijoittelu/jatkuva/{}",
              hakuOid);
          result.setErrorResult(
              ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                  .body("Haun aktiiviset sijoittelut -palvelukutsu on aikakatkaistu"));
        });

    Collection<? extends GrantedAuthority> userRoles = SecurityUtil.getRoles();

    Observable.fromFuture(tarjontaResource.haeHaku(hakuOid))
        .subscribe(
            haku -> {
              boolean isAuthorizedForHaku =
                  containsOphRole(userRoles)
                      || authorityCheckService.isAuthorizedForAnyParentOid(
                          haku.tarjoajaOids,
                          userRoles,
                          Collections.singleton("ROLE_APP_SIJOITTELU_CRUD"));

              if (isAuthorizedForHaku) {
                String resp = jatkuvaTilaAutorisoituOrganisaatiolle(hakuOid);
                result.setResult(ResponseEntity.status(HttpStatus.OK).body(resp));
              } else {
                String msg =
                    String.format(
                        "Käyttäjällä ei oikeutta haun %s haun tarjoajiin %s tai niiden yläorganisaatioihin.",
                        hakuOid, String.join(", ", haku.tarjoajaOids));
                LOG.error(msg);
                result.setErrorResult(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg));
              }
            });

    return result;
  }

  private boolean containsOphRole(Collection<? extends GrantedAuthority> userRoles) {
    for (GrantedAuthority auth : userRoles) {
      if (OPH_CRUD_ROLE.equals(auth.getAuthority())) return true;
    }

    return false;
  }

  private String jatkuvaTilaAutorisoituOrganisaatiolle(String hakuOid) {
    if (StringUtils.isBlank(hakuOid)) {
      return null;
    } else {
      JatkuvaSijoittelu jatkuvaSijoittelu = sijoittelunSeurantaResource.hae(hakuOid);
      return new Gson().toJson(jatkuvaSijoittelu);
    }
  }

  @GetMapping(value = "/jatkuva/paivita", produces = MediaType.TEXT_PLAIN_VALUE)
  @PreAuthorize(ANY_CRUD)
  @Operation(
      summary = "Ajastetun sijoittelun aloituksen päivitys",
      responses = {
        @ApiResponse(
            responseCode = "OK",
            content = @Content(schema = @Schema(implementation = String.class)))
      })
  public ResponseEntity<String> paivitaJatkuvanSijoittelunAloitus(
      @RequestParam(value = "hakuOid", required = false) String hakuOid,
      @RequestParam(value = "aloitusajankohta", required = false) Long aloitusajankohta,
      @RequestParam(value = "ajotiheys", required = false) Integer ajotiheys) {
    if (!hakuParametritService.getParametritForHaku(hakuOid).valinnanhallintaEnabled()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("no privileges.");
    }
    try {
      tarkistaJatkuvaSijoittelunParametrit(hakuOid, ajotiheys, aloitusajankohta);
    } catch(IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
    authorityCheckService.checkAuthorizationForHaku(
        hakuOid, Collections.singleton("ROLE_APP_SIJOITTELU_CRUD"));

    sijoittelunSeurantaResource.paivitaSijoittelunAloitusajankohta(
        hakuOid, aloitusajankohta, ajotiheys);
    return ResponseEntity.ok("Sijoittelun ajastus päivitetty");
  }

  @PostMapping(value = "/jatkuva", produces = MediaType.TEXT_PLAIN_VALUE)
  @PreAuthorize(ANY_CRUD)
  @Operation(
          summary = "Ajastetun sijoittelun luonti ja aktivointi",
          responses = {
                  @ApiResponse(
                          responseCode = "OK",
                          content = @Content(schema = @Schema(implementation = String.class)))
          })
  public ResponseEntity<String> luoJatkuvaSijoittelu(
          @RequestBody LuoJatkuvaSijoittelu jatkuva) {
    if (!hakuParametritService.getParametritForHaku(jatkuva.hakuOid).valinnanhallintaEnabled()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("no privileges.");
    }
    try {
      tarkistaJatkuvaSijoittelunParametrit(jatkuva.hakuOid, jatkuva.ajotiheys, jatkuva.aloitusajankohta);
    } catch(IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
    authorityCheckService.checkAuthorizationForHaku(
            jatkuva.hakuOid, Collections.singleton("ROLE_APP_SIJOITTELU_CRUD"));

    sijoittelunSeurantaResource.luoJatkuvaSijoittelu(
            jatkuva.hakuOid, jatkuva.aloitusajankohta, jatkuva.ajotiheys);
    return ResponseEntity.ok("Sijoittelun ajastus luotu");
  }

  public void tarkistaJatkuvaSijoittelunParametrit(String hakuOid, Integer ajotiheys, Long aloitusajankohta) {
    if (StringUtils.isBlank(hakuOid)) {
      throw new IllegalArgumentException("HakuOid puuttuu");
    } else if (ajotiheys == null || ajotiheys > 24) {
      throw new IllegalArgumentException(String.format("Ajotiheys on virheellinen: %s", ajotiheys));
    } else if (aloitusajankohta == null) {
      throw new IllegalArgumentException("Aloitusajankohta puuttuu");
    }
  }
}
