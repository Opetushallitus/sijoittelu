package fi.vm.sade.sijoittelu.tulos.resource;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.CRUD;
import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.READ;
import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.UPDATE;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.HakemusDTO;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveenValintatapajonoDTO;
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
    public List<HakemusDTO> getHakemusBySijoitteluajo(String hakuOid, String sijoitteluajoId, String hakemusOid) {
        if (LATEST.equals(sijoitteluajoId)) {
            return sijoitteluTulosService.haeLatestHakukohteetJoihinHakemusOsallistuu(hakuOid, hakemusOid);
        } else {
            return sijoitteluTulosService.haeHakukohteetJoihinHakemusOsallistuu(Long.parseLong(sijoitteluajoId),
                    hakemusOid);
        }
    }

    @Secured({ READ, UPDATE, CRUD })
    public SijoitteluDTO getSijoitteluByHakuOid(String hakuOid) {
        return sijoitteluTulosService.getSijoitteluByHakuOid(hakuOid);
    }

    @Secured({ READ, UPDATE, CRUD })
    public SijoitteluajoDTO getSijoitteluajo(String hakuOid, String sijoitteluajoId) {
        if (LATEST.equals(sijoitteluajoId)) {
            return sijoitteluTulosService.getLatestSijoitteluajo(hakuOid);
        } else {
            return sijoitteluTulosService.getSijoitteluajo(Long.parseLong(sijoitteluajoId));
        }
    }

    @Secured({ READ, UPDATE, CRUD })
    public HakukohdeDTO getHakukohdeBySijoitteluajo(String hakuOid, String sijoitteluajoId, String hakukohdeOid) {
        if (LATEST.equals(sijoitteluajoId)) {
            return sijoitteluTulosService.getLatestHakukohdeBySijoitteluajo(hakuOid, hakukohdeOid);
        } else {
            return sijoitteluTulosService.getHakukohdeBySijoitteluajo(Long.parseLong(sijoitteluajoId), hakukohdeOid);
        }
    }

    @Secured({ READ, UPDATE, CRUD })
    public List<HakijaDTO> koulutuspaikalliset(String hakuOid, String sijoitteluajoId) {
        if (LATEST.equals(sijoitteluajoId)) {
            return raportointiService.latestKoulutuspaikalliset(hakuOid);
        } else {
            return raportointiService.koulutuspaikalliset((Long.parseLong(sijoitteluajoId)));
        }
    }

    @Secured({ READ, UPDATE, CRUD })
    public Collection<HakijaDTO> koulutuspaikalliset(String hakuOid, final String hakukohdeOid, String sijoitteluajoId) {
        return Collections2.filter(koulutuspaikalliset(hakuOid, sijoitteluajoId), new Predicate<HakijaDTO>() {
            public boolean apply(HakijaDTO hakija) {
                return onkoHyvaksyttynaHakukohteenseen(hakija, hakukohdeOid);
            }
        });
    }

    private boolean onkoHyvaksyttynaHakukohteenseen(HakijaDTO hakija, String hakukohdeOid) {
        for (HakutoiveDTO hakutoive : hakija.getHakutoiveet()) {
            // voi olla hyvaksytty useampaan hakukohteeseen joten kaikki kohteet
            // kaytava lapi!
            if (hakukohdeOid.equals(hakutoive.getHakukohdeOid())) {
                for (HakutoiveenValintatapajonoDTO jono : hakutoive.getHakutoiveenValintatapajonot()) {
                    // hyvaksytty EDES yhdessa jonossa
                    if (HakemuksenTila.HYVAKSYTTY.equals(jono.getTila())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Secured({ READ, UPDATE, CRUD })
    public List<HakijaDTO> ilmankoulutuspaikkaa(String hakuOid, String sijoitteluajoId) {
        if (LATEST.equals(sijoitteluajoId)) {
            return raportointiService.latestIlmankoulutuspaikkaa(hakuOid);
        } else {
            return raportointiService.ilmankoulutuspaikkaa(Long.parseLong(sijoitteluajoId));
        }
    }

    @Override
    public List<HakijaDTO> hakijat(@PathParam("hakuOid") String hakuOid,
            @PathParam("sijoitteluajoId") String sijoitteluajoId) {
        if ("latest".equals(sijoitteluajoId)) {
            return raportointiService.latestHakijat(hakuOid);
        } else {
            return raportointiService.hakijat(Long.parseLong(sijoitteluajoId));
        }
    }

}
