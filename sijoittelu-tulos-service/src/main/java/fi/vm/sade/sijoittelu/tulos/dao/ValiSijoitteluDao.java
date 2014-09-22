package fi.vm.sade.sijoittelu.tulos.dao;

import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.ValiSijoittelu;

import java.util.Optional;

public interface ValiSijoitteluDao {
	void persistSijoittelu(ValiSijoittelu sijoittelu);

	Optional<ValiSijoittelu> getSijoitteluByHakuOid(String hakuOid);

	Optional<SijoitteluAjo> getSijoitteluajo(Long sijoitteluajoId);

    Optional<SijoitteluAjo> getLatestSijoitteluajo(String hakuOid);
}
