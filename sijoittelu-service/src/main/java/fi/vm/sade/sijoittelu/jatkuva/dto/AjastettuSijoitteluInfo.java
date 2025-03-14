package fi.vm.sade.sijoittelu.jatkuva.dto;

import fi.vm.sade.sijoittelu.jatkuva.util.Formatter;
import java.util.Date;

public class AjastettuSijoitteluInfo {
  private final String hakuOid;
  private final String alkuAika;
  private final Integer interval;

  public AjastettuSijoitteluInfo(String hakuOid, Date alkuAika, Integer interval) {
    this.hakuOid = hakuOid;
    this.alkuAika = Formatter.paivamaara(alkuAika);
    this.interval = interval;
  }

  public String getHakuOid() {
    return hakuOid;
  }

  public String getAlkuAika() {
    return alkuAika;
  }

  public Integer getInterval() {
    return interval;
  }
}
