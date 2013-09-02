/*package fi.vm.sade.sijoittelu.tulos.converter;

import java.sql.Date;

import org.springframework.core.convert.converter.Converter;

import fi.vm.sade.generic.common.DateHelper;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.tulos.service.types.tulos.SijoitteluajoTyyppi;

public class SijoitteluajoToSijoitteluajoTyyppiConverter implements Converter<SijoitteluAjo, SijoitteluajoTyyppi> {

    @Override
    public SijoitteluajoTyyppi convert(SijoitteluAjo source) {
        SijoitteluajoTyyppi converted = new SijoitteluajoTyyppi();
        converted.setSijoitteluId(source.getSijoitteluajoId());
        if (source.getStartMils() != null) {
            converted.setAloitusaika(DateHelper.DateToXmlCal(new Date(source.getStartMils())));
        }
        if (source.getEndMils() != null) {
            converted.setPaattymisaika(DateHelper.DateToXmlCal(new Date(source.getEndMils())));
        }
        for (HakukohdeItem h : source.getHakukohteet()) {
            converted.getHakukohdeOids().add(h.getOid());
        }
        return converted;
    }
}
  */