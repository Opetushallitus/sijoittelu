package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HenkiloWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakijaryhma;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SijoitteluajoWrapperFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluajoWrapperFactory.class);

    public static SijoitteluajoWrapper createSijoitteluAjoWrapper(SijoitteluConfiguration sijoitteluConfiguration,
                                                                  SijoitteluAjo sijoitteluAjo,
                                                                  List<Hakukohde> hakukohteet,
                                                                  Map<String, Map<String, ValintatapajonoDTO>> hakukohdeMapToValintatapajonoByOid) {
        LOG.info(String.format("Luodaan SijoitteluAjoWrapper haulle %s konfiguraatiolla %s",
            sijoitteluAjo.getHakuOid(), sijoitteluConfiguration));
        SijoitteluajoWrapper sijoitteluajoWrapper = new SijoitteluajoWrapper(sijoitteluConfiguration, sijoitteluAjo);
        Map<String, HenkiloWrapper> hakemusOidMap = new HashMap<>();
        hakukohteet.forEach(hakukohde -> {
            HakukohdeWrapper hakukohdeWrapper = new HakukohdeWrapper();
            hakukohdeWrapper.setHakukohde(hakukohde);
            sijoitteluajoWrapper.getHakukohteet().add(hakukohdeWrapper);
            hakukohdeWrapper.setSijoitteluajoWrapper(sijoitteluajoWrapper);
            hakukohde.getValintatapajonot().forEach(valintatapajono -> {
                ValintatapajonoWrapper valintatapajonoWrapper = new ValintatapajonoWrapper();
                valintatapajonoWrapper.setValintatapajono(valintatapajono);
                if (!hakukohdeMapToValintatapajonoByOid.isEmpty()) {
                    valintatapajonoWrapper.setMerkitseMyohAuto(hakukohdeMapToValintatapajonoByOid
                            .get(hakukohde.getOid())
                            .get(valintatapajono.getOid())
                            .getMerkitseMyohAuto());
                }
                hakukohdeWrapper.getValintatapajonot().add(valintatapajonoWrapper);
                valintatapajonoWrapper.setHakukohdeWrapper(hakukohdeWrapper);
                valintatapajono.getHakemukset().forEach(hakemus -> {
                    HakemusWrapper hakemusWrapper = new HakemusWrapper();
                    hakemusWrapper.setHakemus(hakemus);
                    valintatapajonoWrapper.getHakemukset().add(hakemusWrapper);
                    hakemusWrapper.setValintatapajono(valintatapajonoWrapper);
                    HenkiloWrapper henkiloWrapper = getOrCreateHenkilo(hakemus, hakemusOidMap);
                    henkiloWrapper.getHakemukset().add(hakemusWrapper);
                    hakemusWrapper.setHenkilo(henkiloWrapper);
                });
            });
            addHakijaRyhmatToHakijaRyhmaWrapper(hakemusOidMap, hakukohde, hakukohdeWrapper);
        });
        LOG.info("SijoitteluAjoWrapper luotu haulle {}", sijoitteluAjo.getHakuOid());
        return sijoitteluajoWrapper;
    }

    private static void addHakijaRyhmatToHakijaRyhmaWrapper(Map<String, HenkiloWrapper> hakemusOidMap, Hakukohde hakukohde, HakukohdeWrapper hakukohdeWrapper) {
        for (Hakijaryhma hakijaryhma : hakukohde.getHakijaryhmat()) {
            HakijaryhmaWrapper hakijaryhmaWrapper = new HakijaryhmaWrapper();
            hakijaryhmaWrapper.setHakijaryhma(hakijaryhma);
            hakijaryhmaWrapper.setHakukohdeWrapper(hakukohdeWrapper);
            hakukohdeWrapper.getHakijaryhmaWrappers().add(hakijaryhmaWrapper);
            for (String oid : hakijaryhma.getHakemusOid()) {
                HenkiloWrapper henkilo = getHenkilo(oid, hakemusOidMap);
                hakijaryhmaWrapper.getHenkiloWrappers().add(henkilo);
            }
        }
    }


    private static HenkiloWrapper getOrCreateHenkilo(Hakemus hakemus, Map<String, HenkiloWrapper> hakemusOidMap) {
        HenkiloWrapper henkiloWrapper = null;
        if (hakemus.getHakemusOid() != null && !hakemus.getHakemusOid().isEmpty()) {
            henkiloWrapper = hakemusOidMap.get(hakemus.getHakemusOid());
        }
        if (henkiloWrapper == null) {
            henkiloWrapper = new HenkiloWrapper();
            henkiloWrapper.setHakemusOid(hakemus.getHakemusOid());
            henkiloWrapper.setHakijaOid(hakemus.getHakijaOid());
            if (hakemus.getHakemusOid() != null && !hakemus.getHakemusOid().isEmpty()) {
                hakemusOidMap.put(hakemus.getHakemusOid(), henkiloWrapper);
            }
        }
        return henkiloWrapper;
    }

    private static HenkiloWrapper getHenkilo(String hakijaOid, Map<String, HenkiloWrapper> hakijaOidMap) {
        return hakijaOidMap.get(hakijaOid);
    }
}
