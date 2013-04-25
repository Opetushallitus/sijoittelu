package fi.vm.sade.sijoittelu.converter;

import org.springframework.core.convert.converter.Converter;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.tulos.service.types.tulos.HakukohdeTilaTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakukohdeTyyppi;

public class HakukohdeToHakukohdeTyyppiConverter implements Converter<Hakukohde, HakukohdeTyyppi> {

    @Override
    public HakukohdeTyyppi convert(Hakukohde source) {
        HakukohdeTyyppi converted = new HakukohdeTyyppi();

        // FIXME: Tarvitaanko nimeä?
        converted.setNimi(source.getOid());
        converted.setOid(source.getOid());
        if (source.getTila() != null) {
            converted.setTila(HakukohdeTilaTyyppi.valueOf(source.getTila().name()));
        }

        ValintatapajonoToValintatapajonoTyyppiConverter valintatapajonoConverter = new ValintatapajonoToValintatapajonoTyyppiConverter();
        for (Valintatapajono valintatapajono : source.getValintatapajonot()) {
            converted.getValintatapajonos().add(valintatapajonoConverter.convert(valintatapajono));
        }

        return converted;
    }
}
