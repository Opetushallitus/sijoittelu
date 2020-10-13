package fi.vm.sade.sijoittelu.laskenta.service.it.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakuV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.OhjausparametriResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.*;
import fi.vm.sade.sijoittelu.laskenta.service.it.Haku;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TarjontaIntegrationServiceImpl implements TarjontaIntegrationService{
    private HakuV1Resource hakuV1Resource;
    private OhjausparametriResource ohjausparametriResource;
    private Gson gson;

    @Autowired
    public TarjontaIntegrationServiceImpl(HakuV1Resource hakuV1Resource, OhjausparametriResource ohjausparametriResource) {
        this.hakuV1Resource = hakuV1Resource;
        this.ohjausparametriResource = ohjausparametriResource;
        this.gson = new GsonBuilder().create();
    }

    @Override
    public Haku getHaku(String hakuOid) {
        HakuDTO tarjontaHaku;
        ParametriDTO ohjausparametrit;
        try {
            tarjontaHaku = hakuV1Resource.findByOid(hakuOid).getResult();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Haun %s haku epäonnistui", hakuOid));
        }
        try {
            ohjausparametrit = this.gson.fromJson(ohjausparametriResource.haePaivamaara(hakuOid), ParametriDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Haun %s ohjausparametrien haku epäonnistui", hakuOid));
        }
        return new Haku(tarjontaHaku, ohjausparametrit);
    }
}
