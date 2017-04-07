package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.auditlog.valintaperusteet.ValintaperusteetOperation;
import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO;
import fi.vm.sade.sijoittelu.laskenta.service.exception.HakemustaEiLoytynytException;
import fi.vm.sade.sijoittelu.laskenta.service.exception.ValintatapajonoaEiLoytynytException;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.vm.sade.auditlog.valintaperusteet.LogMessage.builder;
import static fi.vm.sade.sijoittelu.laskenta.util.SijoitteluAudit.AUDIT;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
public class SijoittelunTilaService {
    private static final Logger LOG = LoggerFactory.getLogger(SijoittelunTilaService.class);
    public static final String OPH_OID = "1.2.246.562.10.00000000001";
    private final String KK_KOHDEJOUKKO = "haunkohdejoukko_12";

    private final int maxAjoMaara;

    private final RaportointiService raportointiService;
    private final Authorizer authorizer;
    private final ValintatulosDao valintatulosDao;
    private final HakukohdeDao hakukohdeDao;
    private final TarjontaIntegrationService tarjontaIntegrationService;

    @Autowired
    public SijoittelunTilaService( @Value("${sijoittelu.maxAjojenMaara:20}") int maxAjoMaara,
                                   RaportointiService raportointiService,
                                   Authorizer authorizer,
                                   ValintatulosDao valintatulosDao,
                                   HakukohdeDao hakukohdeDao,
                                   TarjontaIntegrationService tarjontaIntegrationService) {
        this.raportointiService = raportointiService;
        this.authorizer = authorizer;
        this.valintatulosDao = valintatulosDao;
        this.hakukohdeDao = hakukohdeDao;
        this.tarjontaIntegrationService = tarjontaIntegrationService;
        this.maxAjoMaara = maxAjoMaara;
    }



    public Valintatulos haeHakemuksenTila(String hakuoid, String hakukohdeOid, String valintatapajonoOid, String hakemusOid) {
        if (isBlank(hakukohdeOid) || isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatulos(hakukohdeOid, valintatapajonoOid, hakemusOid);
    }

    public List<Valintatulos> haeHakemustenTilat(String hakukohdeOid, String valintatapajonoOid) {
        if (isBlank(hakukohdeOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatulokset(hakukohdeOid, valintatapajonoOid);
    }

    public List<Valintatulos> haeHakukohteenTilat(String hakukohdeOid) {
        if (isBlank(hakukohdeOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatuloksetForHakukohde(hakukohdeOid);
    }

    public List<Valintatulos> haeHakemuksenTila(String hakemusOid) {
        if (isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatulos(hakemusOid);
    }

    public Hakukohde getHakukohde(String hakuOid, String hakukohdeOid) {
        return raportointiService.cachedLatestSijoitteluAjoForHakukohde(hakuOid, hakukohdeOid)
                .map(sijoitteluAjo -> hakukohdeDao.getHakukohdeForSijoitteluajo(sijoitteluAjo.getSijoitteluajoId(), hakukohdeOid))
                .orElseThrow(() -> new RuntimeException("Sijoittelua ei löytynyt haulle: " + hakuOid));
    }

    public Valintatapajono getValintatapajono(String valintatapajonoOid, Hakukohde hakukohde) {
        return hakukohde.getValintatapajonot().stream()
                .filter(v -> valintatapajonoOid.equals(v.getOid()))
                .findFirst()
                .orElseThrow(() -> new ValintatapajonoaEiLoytynytException(String.format("Valintatapajonoa %sei löytynyt hakukohteelle %s", valintatapajonoOid, hakukohde.getOid())));
    }

    public void vaihdaHakemuksenTila(String hakuoid, Hakukohde hakukohde, Valintatulos change, String selite, String muokkaaja) {
        String valintatapajonoOid = change.getValintatapajonoOid();
        String hakemusOid = change.getHakemusOid();
        if (isBlank(hakuoid) || isBlank(valintatapajonoOid) || isBlank(hakemusOid)) {
            throw new IllegalArgumentException(String.format("hakuoid: %s, valintatapajonoOid: %s, hakemusOid: %s", hakuoid, valintatapajonoOid, hakemusOid));
        }
        String tarjoajaOid = hakukohde.getTarjoajaOid();
        if (isBlank(tarjoajaOid)) {
            updateMissingTarjoajaOidFromTarjonta(hakukohde, hakuoid);
        }
        // Oph-admin voi muokata aina
        // organisaatio updater voi muokata, jos hyväksytty
        authorizer.checkOrganisationAccess(tarjoajaOid, SijoitteluRole.UPDATE_ROLE, SijoitteluRole.CRUD_ROLE);

        Valintatapajono valintatapajono = getValintatapajono(valintatapajonoOid, hakukohde);
        Hakemus hakemus = getHakemus(hakemusOid, valintatapajono);
        String hakukohdeOid = hakukohde.getOid();
        Valintatulos v = Optional.ofNullable(valintatulosDao.loadValintatulos(hakukohdeOid, valintatapajonoOid, hakemusOid))
                .orElse(new Valintatulos(
                        valintatapajono.getOid(),
                        hakemus.getHakemusOid(),
                        hakukohde.getOid(),
                        hakemus.getHakijaOid(),
                        hakuoid,
                        hakemus.getPrioriteetti()));
        if (notModifying(change, v)) {
            return;
        }
        if (v.getViimeinenMuutos() != null && v.getViimeinenMuutos().after(change.getRead())) {
            throw new StaleReadException(hakuoid, hakukohdeOid, valintatapajonoOid, hakemusOid, v.getViimeinenMuutos(), change.getRead());
        }

        authorizeJulkaistavissa(hakuoid, v.getJulkaistavissa(), change.getJulkaistavissa());
        authorizeHyvaksyPeruuntunutModification(tarjoajaOid, change.getHyvaksyPeruuntunut(), v);

        LOG.info("Muutetaan valintatulosta hakukohdeoid {}, valintatapajonooid {}, hakemusoid {}: {}",
                hakukohdeOid, valintatapajonoOid, hakemusOid, muutos(v, change));
        if(change.getIlmoittautumisTila() != null) {
            v.setIlmoittautumisTila(change.getIlmoittautumisTila(), selite, muokkaaja);
        }
        v.setJulkaistavissa(change.getJulkaistavissa(), selite, muokkaaja);
        v.setEhdollisestiHyvaksyttavissa(change.getEhdollisestiHyvaksyttavissa(), selite, muokkaaja);
        if (change.getEhdollisestiHyvaksyttavissa() && change.getEhdollisenHyvaksymisenEhtoKoodi() == null){
            throw new IllegalArgumentException("Ehdollisenehdollisen hyvaksymisen syyn koodi puuttuu. " + String.format("hakuoid: %s, valintatapajonoOid: %s, hakemusOid: %s", hakuoid, valintatapajonoOid, hakemusOid));
        }
        if (isEhdollisestihyvaksyttavissaMuuWithoutReasons(change)) {
            throw new IllegalArgumentException("Ehdollisen hyväksymisen koodi on 'muu', mutta syytä ei oltu määritelty kaikilla kielillä. " +
                    String.format("hakuoid: %s, valintatapajonoOid: %s, hakemusOid: %s", hakuoid, valintatapajonoOid, hakemusOid));
        }
        v.setEhdollisenHyvaksymisenEhtoKoodi(change.getEhdollisenHyvaksymisenEhtoKoodi(), selite, muokkaaja);
        v.setEhdollisenHyvaksymisenEhtoFI(change.getEhdollisenHyvaksymisenEhtoFI(), selite, muokkaaja);
        v.setEhdollisenHyvaksymisenEhtoSV(change.getEhdollisenHyvaksymisenEhtoSV(), selite, muokkaaja);
        v.setEhdollisenHyvaksymisenEhtoEN(change.getEhdollisenHyvaksymisenEhtoEN(), selite, muokkaaja);
        v.setHyvaksyttyVarasijalta(change.getHyvaksyttyVarasijalta(), selite, muokkaaja);
        v.setHyvaksyPeruuntunut(change.getHyvaksyPeruuntunut(), selite, muokkaaja);
        v.setHyvaksymiskirjeLahetetty(change.getHyvaksymiskirjeLahetetty(), selite, muokkaaja);
        valintatulosDao.createOrUpdateValintatulos(v);

        AUDIT.log(builder()
                .id(muokkaaja)
                .hakuOid(hakuoid)
                .hakukohdeOid(hakukohde.getOid())
                .hakemusOid(v.getHakemusOid())
                .valintatapajonoOid(v.getValintatapajonoOid())
                .add("ilmoittautumistila", v.getIlmoittautumisTila())
                .add("julkaistavissa", v.getJulkaistavissa())
                .add("hyvaksymiskirjeLahetetty", v.getHyvaksymiskirjeLahetetty())
                .add("hyvaksyttyvarasijalta", v.getHyvaksyttyVarasijalta())
                .add("hyvaksyperuuntunut", v.getHyvaksyPeruuntunut())
                .add("ehdollisestihyvaksyttavissa", v.getEhdollisestiHyvaksyttavissa())
                .add("ehdollisenHyvaksymisenEhtoKoodi", v.getEhdollisenHyvaksymisenEhtoKoodi())
                .add("ehdollisenHyvaksymisenEhtoFI", v.getEhdollisenHyvaksymisenEhtoFI())
                .add("ehdollisenHyvaksymisenEhtoSV", v.getEhdollisenHyvaksymisenEhtoSV())
                .add("ehdollisenHyvaksymisenEhtoEN", v.getEhdollisenHyvaksymisenEhtoEN())
                .add("selite", selite)
                .setOperaatio(ValintaperusteetOperation.HAKEMUS_TILAMUUTOS)
                .build());
    }

    private boolean isEhdollisestihyvaksyttavissaMuuWithoutReasons(Valintatulos change) {
        return change.getEhdollisestiHyvaksyttavissa() &&
                change.getEhdollisenHyvaksymisenEhtoKoodi() != null &&
                change.getEhdollisenHyvaksymisenEhtoKoodi().equals(EhdollisenHyvaksymisenEhtoKoodi.EHTO_MUU) &&
                (StringUtils.isEmpty(change.getEhdollisenHyvaksymisenEhtoFI()) ||
                        StringUtils.isEmpty(change.getEhdollisenHyvaksymisenEhtoSV()) ||
                        StringUtils.isEmpty(change.getEhdollisenHyvaksymisenEhtoEN()));
    }

    private void authorizeJulkaistavissa(String hakuOid, boolean v, boolean change) {
        if (!v && change && !KK_KOHDEJOUKKO.equals(tarjontaIntegrationService.getHaunKohdejoukko(hakuOid).orElse(""))) {
            ParametriArvoDTO d = tarjontaIntegrationService.getHaunParametrit(hakuOid).getPH_VEH();
            if (d.getDate() == null || d.getDate() > new Date().getTime()) {
                authorizer.checkOrganisationAccess(OPH_OID, SijoitteluRole.CRUD_ROLE);
            }
        }
    }

    private void authorizeHyvaksyPeruuntunutModification(String tarjoajaOid, boolean hyvaksyPeruuntunut, Valintatulos v) {
        if (v.getHyvaksyPeruuntunut() != hyvaksyPeruuntunut) {
            String hyvaksymisTila = hyvaksyPeruuntunut ? " hyvaksyi peruuntuneen " : " perui hyvaksynnan peruuntuneelta ";
            LOG.info(getUsernameFromSession() + hyvaksymisTila + v.getHakijaOid() + " hakemuksen " + v.getHakemusOid());
            authorizer.checkOrganisationAccess(tarjoajaOid, SijoitteluRole.PERUUNTUNEIDEN_HYVAKSYNTA);
        }
    }

    private String getUsernameFromSession() {
        Authentication authentication = getContext().getAuthentication();
        return authentication == null ? "[No user defined in session]" : authentication.getName();
    }

    public void asetaJononValintaesitysHyvaksytyksi(Hakukohde hakukohde, String valintatapajonoOid, boolean hyvaksytty, String hakuOid) {
        LOG.info("Asetetaan hakukohteen {} valintatapajonon {} valintaesitys hyväksytyksi: {}", hakukohde.getOid(), valintatapajonoOid, hyvaksytty);
        Valintatapajono valintatapajono = getValintatapajono(valintatapajonoOid, hakukohde);
        valintatapajono.setValintaesitysHyvaksytty(hyvaksytty);
        hakukohdeDao.persistHakukohde(hakukohde, hakuOid);
    }

    public int getMaxAjoMaara() {
        return maxAjoMaara;
    }

    private void updateMissingTarjoajaOidFromTarjonta(Hakukohde hakukohde, String hakuOid) {
        String oid = tarjontaIntegrationService.getTarjoajaOid(hakukohde.getOid())
                .orElseThrow(() -> new RuntimeException("Hakukohteelle " + hakukohde.getOid() + " ei löytynyt tarjoajaOidia sijoitteluajosta: " + hakukohde.getSijoitteluajoId()));
        hakukohde.setTarjoajaOid(oid);
        hakukohdeDao.persistHakukohde(hakukohde, hakuOid);
    }

    private Hakemus getHakemus(final String hakemusOid, final Valintatapajono valintatapajono) {
        return valintatapajono.getHakemukset().stream()
                .filter(h -> hakemusOid.equals(h.getHakemusOid()))
                .findFirst()
                .orElseThrow(() -> new HakemustaEiLoytynytException(valintatapajono.getOid(), hakemusOid));
    }

    private String muutos(Valintatulos v, Valintatulos change) {
        IlmoittautumisTila ilmoittautumisTila = change.getIlmoittautumisTila();
        boolean julkaistavissa = change.getJulkaistavissa();
        boolean hyvaksyttyVarasijalta = change.getHyvaksyttyVarasijalta();
        boolean hyvaksyPeruuntunut = change.getHyvaksyPeruuntunut();
        boolean ehdollinenHyvaksynta = change.getEhdollisestiHyvaksyttavissa();
        List<String> muutos = new ArrayList<>();
        if (ilmoittautumisTila != null && ilmoittautumisTila != v.getIlmoittautumisTila()) {
            muutos.add(Optional.ofNullable(v.getIlmoittautumisTila()).map(Enum::name).orElse("") + " -> " + ilmoittautumisTila.name());
        }
        if (julkaistavissa != v.getJulkaistavissa()) {
            muutos.add((v.getJulkaistavissa() ? "JULKAISTAVISSA" : "EI JULKAISTAVISSA") + " -> " + (julkaistavissa ? "JULKAISTAVISSA" : "EI JULKAISTAVISSA"));
        }
        if (hyvaksyttyVarasijalta != v.getHyvaksyttyVarasijalta()) {
            muutos.add((v.getHyvaksyttyVarasijalta() ? "HYVÄKSYTTY VARASIJALTA" : "") + " -> " + (hyvaksyttyVarasijalta ? "HYVÄKSYTTY VARASIJALTA" : ""));
        }
        if (hyvaksyPeruuntunut != v.getHyvaksyPeruuntunut()) {
            muutos.add((v.getHyvaksyPeruuntunut() ? "HYVÄKSYTTY PERUUNTUNUT" : "") + " -> " + (hyvaksyPeruuntunut ? "HYVÄKSYTTY PERUUNTUNUT" : ""));
        }
        if (ehdollinenHyvaksynta != v.getEhdollisestiHyvaksyttavissa()) {
            muutos.add((v.getEhdollisestiHyvaksyttavissa() ? "EHDOLLINEN HYVÄKSYNTÄ" : "") + " -> " + (ehdollinenHyvaksynta ? "EHDOLLINEN HYVÄKSYNTÄ" : ""));
        }
        return muutos.stream().collect(Collectors.joining(", "));
    }

    private boolean notModifying(Valintatulos change, Valintatulos v) {
        return v.getTila() == change.getTila() &&
                v.getIlmoittautumisTila() == change.getIlmoittautumisTila() &&
                v.getJulkaistavissa() == change.getJulkaistavissa() &&
                v.getHyvaksyttyVarasijalta() == change.getHyvaksyttyVarasijalta() &&
                v.getHyvaksyPeruuntunut() == change.getHyvaksyPeruuntunut();
    }
}
