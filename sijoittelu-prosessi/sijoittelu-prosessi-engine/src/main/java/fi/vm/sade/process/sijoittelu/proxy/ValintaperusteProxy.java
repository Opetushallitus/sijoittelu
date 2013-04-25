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
import fi.vm.sade.service.sijoittelu.schema.HakukohdeTyyppi;
import fi.vm.sade.service.sijoittelu.schema.TarjontaTyyppi;
import fi.vm.sade.service.valintaperusteet.ValintaperusteService;
import fi.vm.sade.service.valintaperusteet.messages.HakuparametritTyyppi;
import fi.vm.sade.service.valintaperusteet.schema.ValintaperusteetTyyppi;
import fi.vm.sade.service.valintaperusteet.schema.ValintatapajonoTyyppi;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eetu Blomqvist
 */
@Component
public class ValintaperusteProxy implements JavaDelegate {

    @Autowired
    ValintaperusteService valintaperusteService;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        TarjontaTyyppi tt = (TarjontaTyyppi) delegateExecution.getVariable("tarjonta");
        List<HakuparametritTyyppi> params = new ArrayList<HakuparametritTyyppi>();

        params.addAll(Lists.transform(tt.getHakukohde(), new Function<HakukohdeTyyppi, HakuparametritTyyppi>() {
            @Override
            public HakuparametritTyyppi apply(@Nullable HakukohdeTyyppi hakukohdeTyyppi) {
                HakuparametritTyyppi p = new HakuparametritTyyppi();
                p.setHakukohdeOid(hakukohdeTyyppi.getOid());
                // FIXME: Mistä tähän saadaan valinnanvaiheet?
                p.setValinnanVaiheJarjestysluku(1);
                return p;
            }
        }));

        List<ValintaperusteetTyyppi> list = valintaperusteService.haeValintaperusteet(params);

        for (HakukohdeTyyppi hakukohdeTyyppi : tt.getHakukohde()) {
            for (ValintaperusteetTyyppi valintaPerusteetTyyppi : list) {

                if (valintaPerusteetTyyppi.getHakukohdeOid().equals(hakukohdeTyyppi.getOid())) {
                    hakukohdeTyyppi.getValintatapajono().addAll(Lists.transform(valintaPerusteetTyyppi.getValintatapajonot(),
                            new Function<ValintatapajonoTyyppi, fi.vm.sade.service.sijoittelu.schema.ValintatapajonoTyyppi>() {
                                @Override
                                public fi.vm.sade.service.sijoittelu.schema.ValintatapajonoTyyppi apply(@Nullable ValintatapajonoTyyppi valintatapajonoTyyppi) {
                                    fi.vm.sade.service.sijoittelu.schema.ValintatapajonoTyyppi vt = new fi.vm.sade.service.sijoittelu.schema.ValintatapajonoTyyppi();
                                    vt.setAloituspaikat(valintatapajonoTyyppi.getAloituspaikat());
                                    vt.setOid(valintatapajonoTyyppi.getOid());
                                    vt.setPrioriteetti(valintatapajonoTyyppi.getPrioriteetti());
                                    //vt.setTila(valintatapajonoTyyppi.getTila());
//                                    vt.getSaanto().addAll(valintatapajonoTyyppi.getSaanto());
                                    return vt;
                                }
                            }));
                }
            }
        }

    }
}
