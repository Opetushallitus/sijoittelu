package fi.vm.sade.sijoittelu.jatkuva.dao;

import fi.vm.sade.sijoittelu.jatkuva.dao.dto.SijoitteluDto;
import java.util.Collection;

public interface JatkuvaSijoitteluDAO {

  SijoitteluDto hae(String hakuOid);

  Collection<SijoitteluDto> hae();

  SijoitteluDto merkkaaSijoittelunAjossaTila(String hakuOid, boolean tila);

  SijoitteluDto merkkaaSijoittelunAjetuksi(String hakuOid);

  void poistaSijoittelu(String hakuOid);

  void paivitaSijoittelunAloitusajankohta(String hakuOid, long aloitusajankohta, int ajotiheys);
}
