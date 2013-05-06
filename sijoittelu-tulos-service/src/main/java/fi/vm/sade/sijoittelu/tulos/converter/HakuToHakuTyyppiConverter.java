package fi.vm.sade.sijoittelu.tulos.converter;

import org.springframework.core.convert.converter.Converter;

import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.tulos.service.types.tulos.HakuTyyppi;

public class HakuToHakuTyyppiConverter implements Converter<Sijoittelu, HakuTyyppi> {

    @Override
    public HakuTyyppi convert(Sijoittelu source) {
        HakuTyyppi converted = new HakuTyyppi();
        // FIXME: Pitäisikö tunniste-kenttää edes olla?
//        converted.setHaunTunniste(source.getSijoitteluId());
        converted.setOid(source.getHakuOid());
 
        for (SijoitteluAjo sa : source.getSijoitteluajot()) {
            converted.getSijoitteluajoIds().add(sa.getSijoitteluajoId());
        }

        return converted;
    }
}
