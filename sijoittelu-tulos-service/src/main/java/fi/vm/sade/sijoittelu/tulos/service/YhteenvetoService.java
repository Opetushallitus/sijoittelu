package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila.*;
import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.Vastaanotettavuustila.*;
import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonTila.KESKEN;
import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonTila.fromHakemuksenTila;

public class YhteenvetoService {

    public static HakemusYhteenvetoDTO yhteenveto(HakijaDTO hakija) {
        return new HakemusYhteenvetoDTO(hakija.getHakemusOid(), hakija.getHakutoiveet().stream().map(hakutoive -> {

            HakutoiveenValintatapajonoDTO jono = getFirst(hakutoive).get();
            YhteenvedonTila valintatila = fromHakemuksenTila(jono.getTila());
            Vastaanotettavuustila vastaanotettavuustila = EI_VASTAANOTETTAVISSA;

            if (Arrays.asList(HYVAKSYTTY, HARKINNANVARAISESTI_HYVAKSYTTY).contains(jono.getTila())) {
                vastaanotettavuustila = VASTAANOTETTAVISSA_SITOVASTI;
                if (hakutoive.getHakutoive() > 1) {
                    // TODO varasijasääntöjen aikaparametri
                    if (false) {

                    } else {
                        Stream<HakutoiveDTO> ylemmatToiveet = ylemmatHakutoiveet(hakija, hakutoive.getHakutoive());
                        boolean ylempiaHakutoiveitaSijoittelematta = ylemmatToiveet.filter(toive -> !toive.isKaikkiJonotSijoiteltu()).count() > 0;
                        if (ylempiaHakutoiveitaSijoittelematta) {
                            valintatila = KESKEN;
                            vastaanotettavuustila = EI_VASTAANOTETTAVISSA;
                        } else {
                            boolean ylempiaHakutoiveitaVaralla = ylemmatToiveet.filter(toive -> getFirst(toive).get().getTila().equals(VARALLA)).count() > 0;
                            if (ylempiaHakutoiveitaVaralla) {
                                vastaanotettavuustila = EI_VASTAANOTETTAVISSA;
                            }
                        }
                    }
                }
            } else {
                if (!hakutoive.isKaikkiJonotSijoiteltu()) {
                    valintatila = KESKEN;
                }
            }
            return new HakutoiveYhteenvetoDTO(hakutoive.getHakukohdeOid(), hakutoive.getTarjoajaOid(), valintatila, jono.getVastaanottotieto(), jono.getIlmoittautumisTila(), vastaanotettavuustila, jono.getVarasijanNumero(), jono.getJonosija());
        }).collect(Collectors.toList()));
    }

    private static Stream<HakutoiveDTO> ylemmatHakutoiveet(HakijaDTO hakija, Integer prioriteettiRaja) {
        return hakija.getHakutoiveet().stream().filter(t -> t.getHakutoive() < prioriteettiRaja);
    }

    private static Optional<HakutoiveenValintatapajonoDTO> getFirst(HakutoiveDTO hakutoive) {
        return hakutoive.getHakutoiveenValintatapajonot()
                .stream()
                .sorted((jono1, jono2) -> fromHakemuksenTila(jono1.getTila()).compareTo(fromHakemuksenTila(jono2.getTila())))
                .findFirst();
    }
}
