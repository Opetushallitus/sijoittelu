package fi.vm.sade.sijoittelu.converter;

import org.springframework.core.convert.converter.Converter;

import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.tulos.service.types.tulos.HakijaTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakijanTilaTyyppi;

public class HakemusToHakijaTyyppiConverter implements Converter<Hakemus, HakijaTyyppi> {

    @Override
    public HakijaTyyppi convert(Hakemus source) {
        HakijaTyyppi converted = new HakijaTyyppi();
        converted.setJonosija(source.getJonosija());
        converted.setOid(source.getHakijaOid());
        // FIXME: Tarvitaanko pisteitä?
        converted.setPisteet(0.0);
        converted.setPrioriteetti(source.getPrioriteetti());
        if (source.getTila() != null) {
            converted.setTila(HakijanTilaTyyppi.valueOf(source.getTila().name()));
        }
        return converted;
    }
} 
