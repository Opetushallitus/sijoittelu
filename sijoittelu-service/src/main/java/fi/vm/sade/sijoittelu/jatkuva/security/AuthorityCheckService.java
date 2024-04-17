package fi.vm.sade.sijoittelu.jatkuva.security;

import static java.util.concurrent.TimeUnit.MINUTES;

import fi.vm.sade.sijoittelu.jatkuva.external.resource.organisaatio.OrganisaatioAsyncResource;
import fi.vm.sade.sijoittelu.jatkuva.external.resource.tarjonta.TarjontaAsyncResource;
import fi.vm.sade.sijoittelu.jatkuva.util.SecurityUtil;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.Set;
import javax.ws.rs.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class AuthorityCheckService {
  private static final Logger LOG = LoggerFactory.getLogger(AuthorityCheckService.class);

  @Autowired private TarjontaAsyncResource tarjontaAsyncResource;
  @Autowired private OrganisaatioAsyncResource organisaatioAsyncResource;

  public void checkAuthorizationForHaku(String hakuOid, Collection<String> requiredRoles) {
    Collection<? extends GrantedAuthority> userRoles = SecurityUtil.getRoles();

    if (SecurityUtil.containsOphRole(userRoles)) {
      // on OPH-käyttäjä, ei tarvitse käydä läpi organisaatioita
      return;
    }

    boolean isAuthorized =
        Observable.fromFuture(tarjontaAsyncResource.haeHaku(hakuOid))
            .map(haku -> isAuthorizedForAnyParentOid(haku.tarjoajaOids, userRoles, requiredRoles))
            .timeout(2, MINUTES)
            .blockingFirst();

    if (!isAuthorized) {
      String msg =
          String.format(
              "Käyttäjällä ei oikeutta haun %s tarjoajaan tai sen yläorganisaatioihin.", hakuOid);
      LOG.error(msg);
      throw new ForbiddenException(msg);
    }
  }

  public boolean isAuthorizedForAnyParentOid(
      Set<String> organisaatioOids,
      Collection<? extends GrantedAuthority> userRoles,
      Collection<String> requiredRoles) {
    try {
      for (String organisaatioOid : organisaatioOids) {
        String parentOidsPath = organisaatioAsyncResource.parentoids(organisaatioOid).get();
        String[] parentOids = parentOidsPath.split("/");

        for (String oid : parentOids) {
          for (String role : requiredRoles) {
            String organizationRole = role + "_" + oid;

            for (GrantedAuthority auth : userRoles) {
              if (organizationRole.equals(auth.getAuthority())) return true;
            }
          }
        }
      }
    } catch (Exception e) {
      String msg =
          String.format(
              "Organisaatioiden %s parentOids -haku epäonnistui",
              String.join(", ", organisaatioOids));
      LOG.error(msg, e);
      throw new ForbiddenException(msg);
    }

    return false;
  }
}
