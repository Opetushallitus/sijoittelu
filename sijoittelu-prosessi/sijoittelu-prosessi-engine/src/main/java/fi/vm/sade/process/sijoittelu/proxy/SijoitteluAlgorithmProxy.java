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

import fi.vm.sade.service.sijoittelu.SijoitteluService;
import fi.vm.sade.service.sijoittelu.schema.TarjontaTyyppi;
import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Eetu Blomqvist
 */
@Component
public class SijoitteluAlgorithmProxy implements JavaDelegate {

    @Autowired
    SijoitteluService sijoitteluAlgorithm;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        SijoitteleTyyppi params = new SijoitteleTyyppi();
        params.setSijoitteluId(delegateExecution.getProcessInstanceId());
        params.setTarjonta((TarjontaTyyppi) delegateExecution.getVariable("tarjonta"));
        sijoitteluAlgorithm.sijoittele(params);
    }
}
