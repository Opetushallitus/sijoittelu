package fi.vm.sade.sijoittelu.tulos.resource;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.READ_UPDATE_CRUD;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaPaginationObject;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;

/**
 * User: wuoti Date: 26.4.2013 Time: 12.41
 */
@Component
@PreAuthorize("isAuthenticated()")
public class SijoitteluResourceImpl implements SijoitteluResource {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(SijoitteluResourceImpl.class);

	@Autowired
	private SijoitteluTulosService sijoitteluTulosService;

    @Autowired
	private RaportointiService raportointiService;

	@Override
	@PreAuthorize(READ_UPDATE_CRUD)
	@ApiOperation(value = "xxxx1", httpMethod = "GET")
	public SijoitteluDTO getSijoitteluByHakuOid(String hakuOid) {
		return sijoitteluTulosService.getSijoitteluByHakuOid(hakuOid);
	}

	@Override
	@PreAuthorize(READ_UPDATE_CRUD)
	@ApiOperation(value = "xxxx2", httpMethod = "GET")
	public SijoitteluajoDTO getSijoitteluajo(String hakuOid,
			String sijoitteluajoId) {
        Optional<SijoitteluAjo> ajo = getSijoitteluAjo(sijoitteluajoId, hakuOid);
        return ajo.map(sijoitteluTulosService::getSijoitteluajo).orElse(new SijoitteluajoDTO());
	}

	@Override
	@PreAuthorize(READ_UPDATE_CRUD)
	@ApiOperation(value = "xxxx3", httpMethod = "GET")
	public Response getHakukohdeBySijoitteluajo(String hakuOid,
			String sijoitteluajoId, String hakukohdeOid) {
        Optional<SijoitteluAjo> sijoitteluAjo = getSijoitteluAjo(sijoitteluajoId, hakuOid);
        return sijoitteluAjo.map(ajo -> {
            HakukohdeDTO hakukohdeBySijoitteluajo = sijoitteluTulosService
                    .getHakukohdeBySijoitteluajo(ajo, hakukohdeOid);
            return Response.ok().entity(hakukohdeBySijoitteluajo).build();
        }).orElse(Response.ok().entity(new HakukohdeDTO()).build());

	}

	@Override
	@PreAuthorize(READ_UPDATE_CRUD)
	@ApiOperation(value = "xxxx3", httpMethod = "GET")
	public HakukohdeDTO getHakukohdeBySijoitteluajoPlainDTO(String hakuOid,
			String sijoitteluajoId, String hakukohdeOid) {
        Optional<SijoitteluAjo> ajo = getSijoitteluAjo(sijoitteluajoId, hakuOid);
        return ajo.map(a -> sijoitteluTulosService.getHakukohdeBySijoitteluajo(a,hakukohdeOid)).orElse(new HakukohdeDTO());
	}


	@Override
	@PreAuthorize(READ_UPDATE_CRUD)
	@ApiOperation(value = "xxxx4", httpMethod = "GET")
	public HakijaPaginationObject hakemukset(String hakuOid,
			String sijoitteluajoId, Boolean hyvaksytyt,
			Boolean ilmanHyvaksyntaa, Boolean vastaanottaneet,
			List<String> hakukohdeOid, Integer count, Integer index) {
		try {
            Optional<SijoitteluAjo> sijoitteluAjo = getSijoitteluAjo(sijoitteluajoId, hakuOid);

            return sijoitteluAjo.map(ajo ->
                raportointiService.hakemukset(ajo, hyvaksytyt,
                        ilmanHyvaksyntaa, vastaanottaneet, hakukohdeOid, count,
                        index)
            ).orElseGet(() -> {
                HakijaPaginationObject vastaus = new HakijaPaginationObject();
                vastaus.setTotalCount(0);
                return vastaus;
            });

		} catch (Exception e) {
			LOGGER.error("Sijoittelun hakemuksia ei saatu! {}", e.getMessage(),
					Arrays.toString(e.getStackTrace()));
            HakijaPaginationObject vastaus = new HakijaPaginationObject();
            vastaus.setTotalCount(0);
            return vastaus;
		}
	}

	@Override
	@PreAuthorize(READ_UPDATE_CRUD)
	@ApiOperation(value = "xxxx5", httpMethod = "GET")
	public HakijaDTO hakemus(@PathParam("hakuOid") String hakuOid,
			@PathParam("sijoitteluajoId") String sijoitteluajoId,
			@PathParam("hakemusOid") String hakemusOid) {

        Optional<SijoitteluAjo> ajo = getSijoitteluAjo(sijoitteluajoId, hakuOid);
        return ajo.map(a -> raportointiService.hakemus(a, hakemusOid)).orElse(new HakijaDTO());
	}

    private Optional<SijoitteluAjo> getSijoitteluAjo(String sijoitteluajoId,
			String hakuOid) {
		if (LATEST.equals(sijoitteluajoId)) {
			return raportointiService.latestSijoitteluAjoForHaku(hakuOid);
		} else {
			return raportointiService.getSijoitteluAjo(Long
					.parseLong(sijoitteluajoId));
		}
	}
}
