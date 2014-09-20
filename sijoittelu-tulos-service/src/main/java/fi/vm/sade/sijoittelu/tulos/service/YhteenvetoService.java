package fi.vm.sade.sijoittelu.tulos.service;

import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.Vastaanotettavuustila.EI_VASTAANOTETTAVISSA;
import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.Vastaanotettavuustila.VASTAANOTETTAVISSA_EHDOLLISESTI;
import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.Vastaanotettavuustila.VASTAANOTETTAVISSA_SITOVASTI;
import static fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonValintaTila.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import fi.vm.sade.sijoittelu.domain.LogEntry;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.*;

@Service
public class YhteenvetoService {

    public static List<HakutoiveenYhteenveto> hakutoiveidenYhteenveto(HakijaDTO hakija, ValintatulosDao valintatulosDao) {
        return hakija.getHakutoiveet().stream().map(hakutoive -> {
            HakutoiveenValintatapajonoDTO jono = getFirst(hakutoive).get();
            YhteenvedonValintaTila valintatila = ifNull(fromHakemuksenTila(jono.getTila()), YhteenvedonValintaTila.KESKEN);
            if (valintatila == VARALLA && jono.isHyvaksyttyVarasijalta()) {
                valintatila = HYVAKSYTTY;
            }
            Vastaanotettavuustila vastaanotettavuustila = EI_VASTAANOTETTAVISSA;
            // Valintatila
            if (Arrays.asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.HARKINNANVARAISESTI_HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY).contains(jono.getTila())) {
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
                            boolean ylempiaHakutoiveitaVaralla = ylemmatHakutoiveet(hakija, hakutoive.getHakutoive()).filter(toive -> getFirst(toive).get().getTila().equals(HakemuksenTila.VARALLA)).count() > 0;
                            if (ylempiaHakutoiveitaVaralla) {
                                vastaanotettavuustila = EI_VASTAANOTETTAVISSA;
                            }
                        }
                    }
                }
            } else {
                if (alempiVastaanotettu(hakija, hakutoive.getHakutoive())) {
                    vastaanotettavuustila = EI_VASTAANOTETTAVISSA;
                    valintatila = PERUUNTUNUT;
                } else if (!hakutoive.isKaikkiJonotSijoiteltu()) {
                    valintatila = KESKEN;
                }
            }

            final YhteenvedonVastaanottotila vastaanottotila = convertVastaanottotila(ifNull(jono.getVastaanottotieto(), ValintatuloksenTila.KESKEN));

            // Vastaanottotilan vaikutus valintatilaan
            if (Arrays.asList(YhteenvedonVastaanottotila.EHDOLLISESTI_VASTAANOTTANUT, YhteenvedonVastaanottotila.VASTAANOTTANUT).contains(vastaanottotila)) {
                valintatila = HYVAKSYTTY;
            } else if (Arrays.asList(YhteenvedonVastaanottotila.PERUNUT).contains(vastaanottotila)) {
                valintatila = PERUNUT;
            } else if (ValintatuloksenTila.ILMOITETTU == jono.getVastaanottotieto()) {
                valintatila = HYVAKSYTTY;
            } else if (Arrays.asList(YhteenvedonVastaanottotila.PERUUTETTU).contains(vastaanottotila)) {
                valintatila = YhteenvedonValintaTila.PERUUTETTU;
            }

            Optional<Date> viimeisinVastaanottotilanMuutos = Optional.empty();
            if (vastaanottotila != YhteenvedonVastaanottotila.KESKEN) {
                vastaanotettavuustila = EI_VASTAANOTETTAVISSA;
                viimeisinVastaanottotilanMuutos = viimeisinVastaanottotilanMuutos(valintatulosDao.loadValintatulos(hakutoive.getHakukohdeOid(), jono.getValintatapajonoOid(), hakija.getHakemusOid()));
            }

            final boolean julkaistavissa = jono.getVastaanottotieto() != ValintatuloksenTila.KESKEN || jono.isJulkaistavissa();

            return new HakutoiveenYhteenveto(hakutoive, jono, valintatila, vastaanottotila, vastaanotettavuustila, julkaistavissa, viimeisinVastaanottotilanMuutos);
        }).collect(Collectors.toList());
    }

    private static boolean alempiVastaanotettu(final HakijaDTO hakija, final Integer hakutoive) {
        return hakija.getHakutoiveet().stream().skip(hakutoive).anyMatch(h ->
            getFirst(h).get().getVastaanottotieto() == ValintatuloksenTila.VASTAANOTTANUT
        );
    }

    public static HakemusYhteenvetoDTO yhteenveto(HakijaDTO hakija, ValintatulosDao valintatulosDao) {
        return new HakemusYhteenvetoDTO(hakija.getHakemusOid(), hakutoiveidenYhteenveto(hakija, valintatulosDao).stream().map(hakutoiveenYhteenveto -> {
            return new HakutoiveYhteenvetoDTO(
                    hakutoiveenYhteenveto.hakutoive.getHakukohdeOid(),
                    hakutoiveenYhteenveto.hakutoive.getTarjoajaOid(),
                    hakutoiveenYhteenveto.valintatila,
                    hakutoiveenYhteenveto.vastaanottotila,
                    ifNull(hakutoiveenYhteenveto.valintatapajono.getIlmoittautumisTila(),IlmoittautumisTila.EI_TEHTY),
                    hakutoiveenYhteenveto.vastaanotettavuustila,
                    hakutoiveenYhteenveto.viimeisinVastaanottotilanMuutos.orElse(null),
                    hakutoiveenYhteenveto.valintatapajono.getJonosija(),
                    hakutoiveenYhteenveto.valintatapajono.getVarasijojaKaytetaanAlkaen(),
                    hakutoiveenYhteenveto.valintatapajono.getVarasijojaTaytetaanAsti(),
                    hakutoiveenYhteenveto.valintatapajono.getVarasijanNumero(),
                    hakutoiveenYhteenveto.julkaistavissa
                    )
            ;
        }).collect(Collectors.toList()));
    }

    private static YhteenvedonVastaanottotila convertVastaanottotila(final ValintatuloksenTila valintatuloksenTila) {
        switch (valintatuloksenTila) {
            case ILMOITETTU:
            case KESKEN: return YhteenvedonVastaanottotila.KESKEN;
            case PERUNUT: return YhteenvedonVastaanottotila.PERUNUT;
            case PERUUTETTU: return YhteenvedonVastaanottotila.PERUUTETTU;
            case EI_VASTAANOTETTU_MAARA_AIKANA: return YhteenvedonVastaanottotila.EI_VASTAANOTETTU_MAARA_AIKANA;
            case EHDOLLISESTI_VASTAANOTTANUT: return YhteenvedonVastaanottotila.EHDOLLISESTI_VASTAANOTTANUT;
            case VASTAANOTTANUT_LASNA:
            case VASTAANOTTANUT_POISSAOLEVA:
            case VASTAANOTTANUT: return YhteenvedonVastaanottotila.VASTAANOTTANUT;
            default: throw new IllegalArgumentException("Unknown state: " + valintatuloksenTila);
        }
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

    private static Optional<Date> viimeisinVastaanottotilanMuutos(Valintatulos valintatulos) {
        if(valintatulos == null) {
            return Optional.empty();
        }
        List<LogEntry> logEntries = valintatulos.getLogEntries();
        int entriesSize = logEntries.size();
        if(entriesSize == 0) {
            return Optional.empty();
        }
        return Optional.of(logEntries.get(entriesSize - 1).getLuotu());
    }

    private static Stream<HakutoiveDTO> ylemmatHakutoiveet(HakijaDTO hakija, Integer prioriteettiRaja) {
        return hakija.getHakutoiveet().stream().filter(t -> t.getHakutoive() < prioriteettiRaja);
    }

    private static Optional<HakutoiveenValintatapajonoDTO> getFirst(HakutoiveDTO hakutoive) {
        return hakutoive.getHakutoiveenValintatapajonot()
                .stream()
                .sorted((jono1, jono2) -> {
                        final YhteenvedonValintaTila tila1 = fromHakemuksenTila(jono1.getTila());
                        final YhteenvedonValintaTila tila2 = fromHakemuksenTila(jono2.getTila());
                        if (tila1 == YhteenvedonValintaTila.VARALLA && tila2 == YhteenvedonValintaTila.VARALLA) {
                            return jono1.getVarasijanNumero() - jono2.getVarasijanNumero();
                        }
                        return tila1.compareTo(tila2);
                }
                )
                .findFirst();
    }
}
