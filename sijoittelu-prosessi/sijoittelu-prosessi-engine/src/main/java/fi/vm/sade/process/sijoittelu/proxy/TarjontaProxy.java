/*
 * Copyright
 * *
 *  Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  European Union Public Licence for more details.
 *
 */

package fi.vm.sade.process.sijoittelu.proxy;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fi.vm.sade.tarjonta.service.TarjontaPublicService;
import fi.vm.sade.tarjonta.service.types.HakukohdeTyyppi;
import fi.vm.sade.tarjonta.service.types.TarjontaTyyppi;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * @author Eetu Blomqvist
 */
@Component
public class TarjontaProxy implements JavaDelegate, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    TarjontaPublicService tarjonta;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        TarjontaTyyppi tarjonta = this.tarjonta.haeTarjonta((String) delegateExecution.getVariable("sijoitteluId"));
        fi.vm.sade.service.sijoittelu.schema.TarjontaTyyppi schemaTyyppi =
                new fi.vm.sade.service.sijoittelu.schema.TarjontaTyyppi();

        schemaTyyppi.setHaku(tarjonta.getHaku());
        schemaTyyppi.getHakukohde().addAll(Lists.transform(tarjonta.getHakukohde(), new Function<HakukohdeTyyppi, fi.vm.sade.service.sijoittelu.schema.HakukohdeTyyppi>() {
            @Override
            public fi.vm.sade.service.sijoittelu.schema.HakukohdeTyyppi apply(@Nullable HakukohdeTyyppi hakukohdeTyyppi) {
                fi.vm.sade.service.sijoittelu.schema.HakukohdeTyyppi ht = new fi.vm.sade.service.sijoittelu.schema.HakukohdeTyyppi();
                ht.setAloituspaikat(hakukohdeTyyppi.getAloituspaikat());
                ht.setHakukelpoisuusVaatimukset(hakukohdeTyyppi.getHakukelpoisuusVaatimukset());
                ht.setHakukohdeNimi(hakukohdeTyyppi.getHakukohdeNimi());
                ht.setHakukohteenHakuOid(hakukohdeTyyppi.getHakukohteenHakuOid());
                ht.setHakukohteenTila(hakukohdeTyyppi.getHakukohteenTila());
                ht.setOid(hakukohdeTyyppi.getOid());
                return ht;
            }
        }));

        delegateExecution.setVariable("tarjonta", schemaTyyppi);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
