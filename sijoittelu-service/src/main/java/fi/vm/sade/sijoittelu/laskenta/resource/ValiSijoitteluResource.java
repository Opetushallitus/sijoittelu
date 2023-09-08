package fi.vm.sade.sijoittelu.laskenta.resource;

import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.ValisijoitteluDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static fi.vm.sade.valintalaskenta.tulos.roles.ValintojenToteuttaminenRole.OPH_CRUD;

@RequestMapping(value = "/resources/valisijoittele")
@RestController
@PreAuthorize("isAuthenticated()")
@Tag(name = "valisijoittele", description = "Resurssi sijoitteluun")
public class ValiSijoitteluResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(ValiSijoitteluResource.class);

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

    @PreAuthorize(OPH_CRUD)
    @PostMapping(value = "/{hakuOid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Välisijoittelun suorittaminen")
    public List<HakukohdeDTO> sijoittele(@PathVariable("hakuOid") String hakuOid, @RequestBody ValisijoitteluDTO hakukohteet) {
        LOGGER.info("Valintatietoja valmistetaan valisijottelulle haussa {}", hakuOid);
        HakuDTO haku = valintatietoService.haeValintatiedotJonoille(hakuOid, hakukohteet.getHakukohteet(), Optional.empty());

        // Asetetaan välisijoittelun vaatimat valintaperusteet
        haku.getHakukohteet().forEach(hakukohde -> {
            hakukohde.getValinnanvaihe().forEach(vaihe -> {
                vaihe.getValintatapajonot().forEach(jono -> {
                    jono.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);
                    jono.setAktiivinen(true);
                    jono.setSiirretaanSijoitteluun(true);
                    jono.setValmisSijoiteltavaksi(true);
                });
            });
        });
        LOGGER.info("Valintatiedot haettu serviceltä haussa {}!", hakuOid);
        Future<List<HakukohdeDTO>> future = sijoitteluBusinessService.valisijoittele(haku);
        try {
            LOGGER.info("############### Odotellaan välisijoittelun valmistumista haussa {} ###############", hakuOid);
            List<HakukohdeDTO> tulokset = future.get(60, TimeUnit.MINUTES);
            LOGGER.info("############### Välisijoittelu valmis haussa {} ###############", hakuOid);
            return tulokset;
        } catch (Exception e) {
            LOGGER.error("Välisijoittelu epäonnistui haulle " + hakuOid, e);
            return new ArrayList<>();
        }
    }
}
