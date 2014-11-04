package fi.vm.sade.sijoittelu.tulos.resource;

import com.wordnik.swagger.annotations.ApiOperation;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaPaginationObject;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.READ_UPDATE_CRUD;


@Component
@PreAuthorize("isAuthenticated()")
public class ErillisSijoitteluResourceImpl implements ErillisSijoitteluResource {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(ErillisSijoitteluResourceImpl.class);

	@Autowired
	private SijoitteluTulosService sijoitteluTulosService;

    @Autowired
	private RaportointiService raportointiService;


	@Override
	@PreAuthorize(READ_UPDATE_CRUD)
	@ApiOperation(value = "xxxx3", httpMethod = "GET")
	public Response getHakukohdeBySijoitteluajo(String hakuOid,
			String sijoitteluajoId, String hakukohdeOid) {

        if(sijoitteluajoId == null) {
            return Response.ok().entity(new HakukohdeDTO()).build();
        }

        SijoitteluAjo ajo = new SijoitteluAjo();
        ajo.setSijoitteluajoId(Long
                .parseLong(sijoitteluajoId));

        HakukohdeDTO hakukohdeBySijoitteluajo = sijoitteluTulosService
                .getHakukohdeBySijoitteluajo(ajo, hakukohdeOid);
        return Response.ok().entity(hakukohdeBySijoitteluajo).build();

	}

}
