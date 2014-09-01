package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.*;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class YhteenvetoService {

    public static HakemusYhteenvetoDTO yhteenveto(HakijaDTO hakija) {
        return new HakemusYhteenvetoDTO(hakija.getHakemusOid(), hakija.getHakutoiveet().stream().map(hakutoive -> {

            HakutoiveenValintatapajonoDTO jono;
            Optional<HakutoiveenValintatapajonoDTO> hyvaksytty = hyvaksytty(hakutoive);
            if (hyvaksytty.isPresent()) {
                /*
                if ylempia hakutoiveita
                     if varasijasääntö lauennut -> hyväksytty, vastaanotto + ehdollinen vastaanotto
                     else
                        if ylempiä hakutoiveita joita ei (kokonaisuudessaan) siirretty sijoitteluun -> kesken
                        else if ylempiä, joissa varalla -> hyväksytty, ei paikan vastaanottoa
                        else hyväksytty -> paikan vastaanotto
                else
                -> hyväksytty, paikan vastaanotto
                */
                jono = hyvaksytty.get();
            } else {
                if (!hakutoive.isKaikkiJonotSijoiteltu()) {
                    jono = hakutoive.getHakutoiveenValintatapajonot().get(0);
                } else {
                    Optional<HakutoiveenValintatapajonoDTO> varalla = varalla(hakutoive);
                    Optional<HakutoiveenValintatapajonoDTO> hylatty = hylatty(hakutoive);
                    if (varalla.isPresent()) {
                        jono = varalla.get();
                    } else { //if (hylatty.isPresent()) {
                        jono = hylatty.get();
                    }
                    // TODO muut tilat?
                    //else {
                    //}
                }
            }
            return new HakutoiveYhteenvetoDTO(hakutoive.getHakukohdeOid(), hakutoive.getTarjoajaOid(), jono.getTila(), jono.getVastaanottotieto(), jono.getIlmoittautumisTila(), jono.getJonosija(), jono.getVarasijanNumero(), hakutoive.isKaikkiJonotSijoiteltu());
        }).collect(Collectors.toList()));
    }

    private static Optional<HakutoiveenValintatapajonoDTO> hylatty(HakutoiveDTO hakutoive) {
        return sisaltaaJononTilassa(hakutoive, HakemuksenTila.HYLATTY);
    }

    private static Optional<HakutoiveenValintatapajonoDTO> hyvaksytty(HakutoiveDTO hakutoive) {
        return sisaltaaJononTilassa(hakutoive, HakemuksenTila.HYVAKSYTTY);
    }

    private static Optional<HakutoiveenValintatapajonoDTO> varalla(HakutoiveDTO hakutoive) {
        return sisaltaaJononTilassa(hakutoive, HakemuksenTila.VARALLA);
    }

    private static Optional<HakutoiveenValintatapajonoDTO> sisaltaaJononTilassa(HakutoiveDTO hakutoive, HakemuksenTila tila) {
        return getFirst(hakutoive, jono -> jono.getTila().equals(tila));
    }

    private static Optional<HakutoiveenValintatapajonoDTO> getFirst(HakutoiveDTO hakutoive, Predicate<HakutoiveenValintatapajonoDTO> predicate) {
        // TODO sort by priority?
        return hakutoive.getHakutoiveenValintatapajonot()
                .stream()
                .filter(predicate)
                .findFirst();
    }
}
