package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.tulos.dto.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila.*;
import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.Vastaanotettavuustila.*;
import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonTila.KESKEN;
import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonTila.fromHakemuksenTila;

import org.joda.time.LocalDate;

public class YhteenvetoService {

    public static HakemusYhteenvetoDTO yhteenveto(HakijaDTO hakija) {
        return new HakemusYhteenvetoDTO(hakija.getHakemusOid(), hakija.getHakutoiveet().stream().map(hakutoive -> {

            HakutoiveenValintatapajonoDTO jono = getFirst(hakutoive).get();
            YhteenvedonTila valintatila = ifNull(fromHakemuksenTila(jono.getTila()), YhteenvedonTila.KESKEN);
            Vastaanotettavuustila vastaanotettavuustila = EI_VASTAANOTETTAVISSA;

            if (Arrays.asList(HYVAKSYTTY, HARKINNANVARAISESTI_HYVAKSYTTY).contains(jono.getTila())) {
                vastaanotettavuustila = VASTAANOTETTAVISSA_SITOVASTI;
                if (hakutoive.getHakutoive() > 1) {
                    if (aikaparametriLauennut(jono)) {
                      vastaanotettavuustila = VASTAANOTETTAVISSA_EHDOLLISESTI;
                    } else {
                        boolean ylempiaHakutoiveitaSijoittelematta = ylemmatHakutoiveet(hakija, hakutoive.getHakutoive()).filter(toive -> !toive.isKaikkiJonotSijoiteltu()).count() > 0;
                        if (ylempiaHakutoiveitaSijoittelematta) {
                            valintatila = KESKEN;
                            vastaanotettavuustila = EI_VASTAANOTETTAVISSA;
                        } else {
                            boolean ylempiaHakutoiveitaVaralla = ylemmatHakutoiveet(hakija, hakutoive.getHakutoive()).filter(toive -> getFirst(toive).get().getTila().equals(VARALLA)).count() > 0;
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
            return new HakutoiveYhteenvetoDTO(hakutoive.getHakukohdeOid(), hakutoive.getTarjoajaOid(), valintatila, ifNull(jono.getVastaanottotieto(), ValintatuloksenTila.KESKEN), ifNull(jono.getIlmoittautumisTila(), IlmoittautumisTila.EI_TEHTY), vastaanotettavuustila, jono.getJonosija(), jono.getVarasijanNumero());
        }).collect(Collectors.toList()));
    }

    private static <T> T ifNull(final T value, final T defaultValue) {
        if (value == null) return defaultValue;
        return value;
    }

    private static boolean aikaparametriLauennut(final HakutoiveenValintatapajonoDTO jono) {
        if (jono.getVarasijojaKaytetaanAlkaen() == null || jono.getVarasijojaTaytetaanAsti() == null) {
            return false;
        }
        final LocalDate alkaen = new LocalDate(jono.getVarasijojaKaytetaanAlkaen());
        final LocalDate asti = new LocalDate(jono.getVarasijojaTaytetaanAsti());
        final LocalDate today = new LocalDate();
        return !today.isBefore(alkaen) && !today.isAfter(asti);
    }

    private static Stream<HakutoiveDTO> ylemmatHakutoiveet(HakijaDTO hakija, Integer prioriteettiRaja) {
        return hakija.getHakutoiveet().stream().filter(t -> t.getHakutoive() < prioriteettiRaja);
    }

    private static Optional<HakutoiveenValintatapajonoDTO> getFirst(HakutoiveDTO hakutoive) {
        return hakutoive.getHakutoiveenValintatapajonot()
                .stream()
                .sorted((jono1, jono2) -> {
                        final YhteenvedonTila tila1 = fromHakemuksenTila(jono1.getTila());
                        final YhteenvedonTila tila2 = fromHakemuksenTila(jono2.getTila());
                        if (tila1 == YhteenvedonTila.VARALLA && tila2 == YhteenvedonTila.VARALLA) {
                            return jono1.getVarasijanNumero() - jono2.getVarasijanNumero();
                        }
                        return tila1.compareTo(tila2);
                }
                )
                .findFirst();
    }
}
