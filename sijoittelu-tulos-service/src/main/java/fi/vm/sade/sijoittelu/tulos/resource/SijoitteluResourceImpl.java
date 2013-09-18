package fi.vm.sade.sijoittelu.tulos.resource;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import fi.vm.sade.sijoittelu.tulos.dto.*;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.*;

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
    public List<HakijaDTO> koulutuspaikalliset(String hakuOid, String sijoitteluajoId) {
        if ("latest".equals(sijoitteluajoId)) {
            return raportointiService.latestKoulutuspaikalliset(hakuOid);
        } else {
            return raportointiService.koulutuspaikalliset((Long.parseLong(sijoitteluajoId)));
        }
    }

    @Secured({ READ, UPDATE, CRUD })
    public List<HakijaDTO> koulutuspaikalliset(String hakuOid, final String hakukohdeOid, String sijoitteluajoId) {
        List<HakijaDTO> hakijat = koulutuspaikalliset(hakuOid, sijoitteluajoId);
        Collections2.filter(hakijat, new Predicate<HakijaDTO>() {
            public boolean apply(HakijaDTO hakija) {
                for (HakutoiveDTO toive : hakija.getHakutoiveet()) {
                    if (HakemuksenTila.HYVAKSYTTY.equals(toive.getTila())) {
                        if (hakukohdeOid.equals(toive.getHakukohdeOid())) {
                            return true; // hyvaksytty oikeaan kohteeseen
                        }
                        return false; // hyvaksytty muuhun kohteeseen
                    }
                }
                return false; // ei hakutoiveita
            }
        });
        return hakijat;
    }

    @Secured({ READ, UPDATE, CRUD })
    public List<HakijaDTO> ilmankoulutuspaikkaa(String hakuOid, String sijoitteluajoId) {
        if ("latest".equals(sijoitteluajoId)) {
            return raportointiService.latestIlmankoulutuspaikkaa(hakuOid);
        } else {
            return raportointiService.ilmankoulutuspaikkaa(Long.parseLong(sijoitteluajoId));
        }
    }

}
