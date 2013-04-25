///*
// * Copyright
// * *
// *  Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
// *
// *  This program is free software:  Licensed under the EUPL, Version 1.1 or - as
// *  soon as they will be approved by the European Commission - subsequent versions
// *  of the EUPL (the "Licence");
// *
// *  You may not use this work except in compliance with the Licence.
// *  You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
// *
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  European Union Public Licence for more details.
// *
// */
//
//package fi.vm.sade.process.sijoittelu.proxy;
//
//import com.google.common.base.Function;
//import com.google.common.collect.Lists;
//import fi.vm.sade.service.sijoittelu.schema.HakukohdeTyyppi;
//import fi.vm.sade.service.sijoittelu.schema.TarjontaTyyppi;
//import fi.vm.sade.service.sijoittelu.schema.ValintatapajonoTyyppi;
//import fi.vm.sade.service.valintaperusteet.messages.HakuparametritTyyppi;
//import fi.vm.sade.service.valintatiedot.ValintatietoService;
//import fi.vm.sade.service.valintatiedot.messages.HakuParametritTyyppi;
//import fi.vm.sade.service.valintatiedot.schema.HakijatTyyppi;
//import org.activiti.engine.delegate.DelegateExecution;
//import org.activiti.engine.delegate.JavaDelegate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Nullable;
//import java.util.List;
//
///**
// * @author Eetu Blomqvist
// */
//@Component
//public class ValintatulosProxy implements JavaDelegate {
//
//    @Autowired
//    ValintatietoService valintatietoService;
//
//    @Override
//    public void execute(DelegateExecution delegateExecution) throws Exception {
//
//        TarjontaTyyppi tarjonta = (TarjontaTyyppi) delegateExecution.getVariable("tarjonta");
//
//        HakuparametritTyyppi params = new HakuparametritTyyppi();
//        params.getHakukohdeOid().addAll(Lists.transform(tarjonta.getHakukohde(), new Function<HakukohdeTyyppi, String>() {
//            @Override
//            public String apply(@Nullable HakukohdeTyyppi hakukohdeTyyppi) {
//                return hakukohdeTyyppi.getOid();
//            }
//        }));
//
//        List<HakijatTyyppi> hakijat = valintatietoService.haeValintatiedot(params);
//
//        // TODO fix correlation when ValintatietoService interface has been refactored. (valintatapajonot)
//
//        for (HakukohdeTyyppi hakukohdeTyyppi : tarjonta.getHakukohde()) {
//            for (HakijatTyyppi hakijatTyyppi : hakijat) {
//                if(hakijatTyyppi.getHakukohdeOid().equals(hakukohdeTyyppi.getOid())){
//
//                    for (ValintatapajonoTyyppi jono : hakukohdeTyyppi.getValintatapajono()) {
//                        jono.getHakija().addAll(hakijatTyyppi.getHakijat());
//                    }
//                }
//            }
//        }
//    }
//}
