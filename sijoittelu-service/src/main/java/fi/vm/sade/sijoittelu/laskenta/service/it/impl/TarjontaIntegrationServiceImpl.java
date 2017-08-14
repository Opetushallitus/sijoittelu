package fi.vm.sade.sijoittelu.laskenta.service.it.impl;

import com.google.gson.GsonBuilder;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakuV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakukohdeV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.OhjausparametriResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.*;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.laskenta.util.HakuUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TarjontaIntegrationServiceImpl implements TarjontaIntegrationService{
    private static final Logger LOG = LoggerFactory.getLogger(TarjontaIntegrationServiceImpl.class);

    @Autowired
    HakuV1Resource hakuV1Resource;

    @Autowired
    HakukohdeV1Resource hakukohdeV1Resource;

    @Autowired
    OhjausparametriResource ohjausparametriResource;

    @Override
    public HakuDTO getHakuByHakuOid(String hakuOid) {
        try {
            return hakuV1Resource.findByOid(hakuOid).getResult();
        } catch (Exception e) {
            final String message = "Hakua " + hakuOid + " ei löytynyt";
            LOG.error(message, e);
            throw new RuntimeException(message);
        }
    }

    @Override
    public ParametriDTO getHaunParametrit(String hakuOid) {
        return new GsonBuilder().create().fromJson(ohjausparametriResource.haePaivamaara(hakuOid), ParametriDTO.class);
    }
}
