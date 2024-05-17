package fi.vm.sade.sijoittelu.jatkuva.parametrit;

import fi.vm.sade.sijoittelu.jatkuva.external.resource.ohjausparametrit.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.jatkuva.external.resource.ohjausparametrit.dto.ParametritDTO;
import java.util.Calendar;
import java.util.Date;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class ParametritParser {

  private ParametritDTO parametrit;
  private String rootOrganisaatioOid;

  public ParametritParser(ParametritDTO parametrit, String rootOrganisaatioOid) {
    this.parametrit = parametrit;
    this.rootOrganisaatioOid = rootOrganisaatioOid;
  }

  public boolean valinnanhallintaEnabled() {
    if (isOPH()) {
      return true;
    }
    return valintapalvelunKayttoEnabled();
  }

  public boolean valintapalvelunKayttoEnabled() {
    if (isOPH()) {
      return true;
    }
    Date now = Calendar.getInstance().getTime();
    ParametriDTO param = this.parametrit.getPH_OLVVPKE();

    if (param != null
        && param.getDateStart() != null
        && param.getDateEnd() != null
        && now.after(param.getDateStart())
        && now.before(param.getDateEnd())) {
      return false;
    } else if (param != null
        && param.getDateStart() != null
        && param.getDateEnd() == null
        && now.after(param.getDateStart())) {
      return false;
    } else if (param != null
        && param.getDateEnd() != null
        && param.getDateStart() == null
        && now.before(param.getDateEnd())) {
      return false;
    }
    return true;
  }

  public boolean koetulostenTallentaminenEnabled() {
    return isAllowedBetween(this.parametrit.getPH_KTT());
  }

  private boolean isAllowedBetween(ParametriDTO param) {
    if (isOPH()) {
      return true;
    }
    Date now = Calendar.getInstance().getTime();
    if (param != null && param.getDateStart() != null && now.before(param.getDateStart())) {
      return false;
    } else if (param != null && param.getDateEnd() != null && now.after(param.getDateEnd())) {
      return false;
    }
    return true;
  }

  private boolean isOPH() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    for (GrantedAuthority authority : authentication.getAuthorities()) {
      if (authority.getAuthority().contains(rootOrganisaatioOid)) {
        return true;
      }
    }
    return false;
  }
}
