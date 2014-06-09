package fi.vm.sade.sijoittelu.tulos.dao;

import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;

public interface SijoitteluCacheDao {

	void persistSijoittelu(Sijoittelu sijoittelu);

	Sijoittelu getSijoitteluByHakuOid(String hakuOid);

	SijoitteluAjo getSijoitteluajo(Long sijoitteluajoId);

	SijoitteluAjo getLatestSijoitteluajo(String hakuOid);
}
