package fi.vm.sade.sijoittelu.jatkuva.dao;

import fi.vm.sade.sijoittelu.jatkuva.dto.JatkuvaSijoittelu;
import java.util.Collection;

public interface JatkuvaSijoitteluDAO {

  JatkuvaSijoittelu hae(String hakuOid);

  Collection<JatkuvaSijoittelu> hae();

  JatkuvaSijoittelu merkkaaSijoittelunAjossaTila(String hakuOid, boolean tila);

  JatkuvaSijoittelu merkkaaSijoittelunAjetuksi(String hakuOid);

  void poistaSijoittelu(String hakuOid);

  void paivitaSijoittelunAloitusajankohta(String hakuOid, long aloitusajankohta, int ajotiheys);
}
