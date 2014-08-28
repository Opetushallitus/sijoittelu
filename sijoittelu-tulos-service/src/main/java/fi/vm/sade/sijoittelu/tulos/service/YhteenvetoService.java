package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakemusYhteenvetoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveYhteenvetoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveenValintatapajonoDTO;

import java.util.stream.Collectors;

public class YhteenvetoService {

    public static HakemusYhteenvetoDTO yhteenveto(HakijaDTO hakija) {
       return new HakemusYhteenvetoDTO(hakija.getHakemusOid(), hakija.getHakutoiveet().stream().map(hakutoive -> {
            HakutoiveenValintatapajonoDTO first = hakutoive.getHakutoiveenValintatapajonot().get(0);
            return new HakutoiveYhteenvetoDTO(hakutoive.getHakukohdeOid(), hakutoive.getTarjoajaOid(), first.getTila(), first.getVastaanottotieto(), first.getIlmoittautumisTila(), first.getJonosija(), first.getVarasijanNumero());
        }).collect(Collectors.toList()));
    }
}
