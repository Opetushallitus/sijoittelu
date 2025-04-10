package fi.vm.sade.sijoittelu.jatkuva.dto;

import java.util.Date;

public class LuoJatkuvaSijoittelu {

  public final String hakuOid;
  public final Long aloitusajankohta;
  public final Integer ajotiheys;

  public LuoJatkuvaSijoittelu() {
    this.hakuOid = null;
    this.aloitusajankohta = null;
    this.ajotiheys = null;
  }

  public LuoJatkuvaSijoittelu(String hakuOid, Long aloitusajankohta, Integer ajotiheys) {
    this.hakuOid = hakuOid;
    this.aloitusajankohta = aloitusajankohta;
    this.ajotiheys = ajotiheys;
  }
}
