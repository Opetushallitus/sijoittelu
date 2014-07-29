package fi.vm.sade.sijoittelu.laskenta.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.ValintalaskentaTulosService;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import java.util.*;

@Path("sijoittele")
@Component
//@PreAuthorize("isAuthenticated()")
@Api(value = "/tila", description = "Resurssi sijoitteluun")
public class SijoitteluResource {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(SijoitteluResource.class);

	@Autowired
	private SijoitteluBusinessService sijoitteluBusinessService;

    @Autowired
    private ValintalaskentaTulosService tulosService;

    @Autowired
    private ValintatietoService valintatietoService;

	@GET
	@Path("{hakuOid}")
//	@PreAuthorize(CRUD)
	@ApiOperation(value = "Hakemuksen valintatulosten haku")
	public String sijoittele(@PathParam("hakuOid") String hakuOid) {

        LOGGER.error("Valintatietoja valmistetaan haulle {}!", hakuOid);

        HakuDTO haku = valintatietoService.haeValintatiedot(hakuOid);
        LOGGER.error("Valintatiedot haettu serviceltä {}!", hakuOid);

//        LOGGER.error("Konvertoidaan hakutyypeiksi {}!", hakuOid);
//        for (HakukohdeDTO v : a) {
//            HakukohdeTyyppi ht = new HakukohdeTyyppi();
//            ht.setOid(v.getOid());
//            ht.setTarjoajaOid(v.getTarjoajaoid());
//            haku.getHakukohteet().add(ht);
//
//            for (ValinnanvaiheDTO valinnanvaiheDTO : v.getValinnanvaihe()) {
//                ht.getValinnanvaihe().add(
//                        createValinnanvaiheTyyppi(valinnanvaiheDTO));
//
//            }
//        }
//        LOGGER.error("Palautetaan valintatiedot {} hakukohteella!", haku
//                .getHakukohteet().size());
        try {
            sijoitteluBusinessService.sijoittele(haku);
            LOGGER.error("Sijoittelu suoritettu onnistuneesti!");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Sijoittelu epäonnistui syystä {}!\r\n{}",
                    e.getMessage(), Arrays.toString(e.getStackTrace()));
            return "false";
        }
        return "true";
	}

}
