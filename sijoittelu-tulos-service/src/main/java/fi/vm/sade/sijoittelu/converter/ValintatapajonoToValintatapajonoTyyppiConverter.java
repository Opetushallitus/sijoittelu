package fi.vm.sade.sijoittelu.converter;

import org.springframework.core.convert.converter.Converter;

import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.tulos.service.types.tulos.ValintatapajonoTyyppi;

public class ValintatapajonoToValintatapajonoTyyppiConverter implements Converter<Valintatapajono, ValintatapajonoTyyppi> {

    @Override
    public ValintatapajonoTyyppi convert(Valintatapajono source) {
        ValintatapajonoTyyppi converted = new ValintatapajonoTyyppi();
        converted.setOid(source.getOid());
        converted.setPrioriteetti(source.getPrioriteetti());

        HakemusToHakijaTyyppiConverter hakemusConverter = new HakemusToHakijaTyyppiConverter();
        for (Hakemus h : source.getHakemukset()) {
            converted.getHakijaList().add(hakemusConverter.convert(h));
        }

        return converted;
    }
}
