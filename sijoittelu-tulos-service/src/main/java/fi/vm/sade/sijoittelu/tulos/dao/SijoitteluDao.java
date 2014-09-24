package fi.vm.sade.sijoittelu.tulos.dao;

import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;

import java.util.List;
import java.util.Optional;

public interface SijoitteluDao {
	void persistSijoittelu(Sijoittelu sijoittelu);

	Optional<Sijoittelu> getSijoitteluByHakuOid(String hakuOid);

    Optional<Sijoittelu> getSijoitteluById(long id);

    List<Sijoittelu> findAll();

	Optional<SijoitteluAjo> getSijoitteluajo(Long sijoitteluajoId);

    Optional<SijoitteluAjo> getLatestSijoitteluajo(String hakuOid);
}
