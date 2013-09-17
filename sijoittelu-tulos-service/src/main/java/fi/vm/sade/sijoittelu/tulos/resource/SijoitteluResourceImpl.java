package fi.vm.sade.sijoittelu.tulos.resource;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.CRUD;
import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.READ;
import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.UPDATE;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import fi.vm.sade.sijoittelu.tulos.dto.HakijaRaportointiDTO;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;

/**
 * User: wuoti Date: 26.4.2013 Time: 12.41
 */
@Component
@PreAuthorize("isAuthenticated()")
public class SijoitteluResourceImpl implements SijoitteluResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluResourceImpl.class);

    @Autowired
    private SijoitteluTulosService sijoitteluTulosService;

    @Autowired
    private RaportointiService raportointiService;

    @Secured({ READ, UPDATE, CRUD })
    public SijoitteluDTO getSijoitteluByHakuOid(String hakuOid) {
        return sijoitteluTulosService.getSijoitteluByHakuOid(hakuOid);
    }

    @Secured({ READ, UPDATE, CRUD })
    public SijoitteluajoDTO getSijoitteluajo(String hakuOid, String sijoitteluajoId) {
        if ("latest".equals(sijoitteluajoId)) {
            return sijoitteluTulosService.getLatestSijoitteluajo(hakuOid);
        } else {
            return sijoitteluTulosService.getSijoitteluajo(Long.parseLong(sijoitteluajoId));
        }
    }

    @Secured({ READ, UPDATE, CRUD })
    public HakukohdeDTO getHakukohdeBySijoitteluajo(String hakuOid, String sijoitteluajoId, String hakukohdeOid) {
        if ("latest".equals(sijoitteluajoId)) {
            return sijoitteluTulosService.getLatestHakukohdeBySijoitteluajo(hakuOid, hakukohdeOid);
        } else {
            return sijoitteluTulosService.getHakukohdeBySijoitteluajo(Long.parseLong(sijoitteluajoId), hakukohdeOid);
        }
    }

    @Secured({ READ, UPDATE, CRUD })
    public List<HakijaRaportointiDTO> koulutuspaikalliset(String hakuOid, String sijoitteluajoId, String hakemusOid) {
        if ("latest".equals(sijoitteluajoId)) {
            return raportointiService.latestKoulutuspaikalliset(hakuOid);
        } else {
            return raportointiService.Koulutuspaikalliset((Long.parseLong(sijoitteluajoId)));
        }
    }

    @Secured({ READ, UPDATE, CRUD })
    public List<HakijaRaportointiDTO> ilmankoulutuspaikkaa(String hakuOid, String sijoitteluajoId, String hakemusOid) {
        if ("latest".equals(sijoitteluajoId)) {
            return raportointiService.latestIlmankoulutuspaikkaa(hakuOid, hakemusOid);
        } else {
            return raportointiService.ilmankoulutuspaikkaa(Long.parseLong(sijoitteluajoId));
        }
    }
}
