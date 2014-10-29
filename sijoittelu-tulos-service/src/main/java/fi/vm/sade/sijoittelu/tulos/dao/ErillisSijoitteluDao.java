package fi.vm.sade.sijoittelu.tulos.dao;

import fi.vm.sade.sijoittelu.domain.ErillisSijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.ValiSijoittelu;

import java.util.Optional;

public interface ErillisSijoitteluDao {
	void persistSijoittelu(ErillisSijoittelu sijoittelu);

	Optional<ErillisSijoittelu> getSijoitteluByHakuOid(String hakuOid);

	Optional<SijoitteluAjo> getSijoitteluajo(Long sijoitteluajoId);

    Optional<SijoitteluAjo> getLatestSijoitteluajo(String hakuOid);
}
