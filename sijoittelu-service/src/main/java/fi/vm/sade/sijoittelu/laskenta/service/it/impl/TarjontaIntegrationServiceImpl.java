package fi.vm.sade.sijoittelu.laskenta.service.it.impl;

import com.google.gson.GsonBuilder;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakuV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakukohdeV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.OhjausparametriResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.*;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by kjsaila on 16/12/14.
 */

@Service
public class TarjontaIntegrationServiceImpl implements TarjontaIntegrationService{

    @Autowired
    HakuV1Resource hakuV1Resource;

    @Autowired
    HakukohdeV1Resource hakukohdeV1Resource;

    @Autowired
    OhjausparametriResource ohjausparametriResource;

    @Override
    public Optional<String> getTarjoajaOid(String hakukohdeOid) {
        try {
            ResultHakukohdeDTO tarjonnanHakukohde = hakukohdeV1Resource.findByOid(hakukohdeOid);
            return tarjonnanHakukohde.getResult().getTarjoajaOids().stream().findFirst();
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Hakukohteelle " + hakukohdeOid + " ei löytynyt tarjoajaOidia");
        }

    }

    @Override
    public Optional<String> getHaunKohdejoukko(String hakuOid) {
        try {
            ResultHakuDTO tarjonnanHaku = hakuV1Resource.findByOid(hakuOid);
            return Optional.ofNullable(tarjonnanHaku.getResult().getKohdejoukkoUri().split("#")[0]);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Haulle " + hakuOid + " ei saatu kohdejoukkoa");
        }
    }

    @Override
    public ParametriDTO getHaunParametrit(String hakuOid) {
        return new GsonBuilder().create().fromJson(ohjausparametriResource.haePaivamaara(hakuOid), ParametriDTO.class);
    }
}
