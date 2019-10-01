package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.*;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeTila;
import fi.vm.sade.sijoittelu.tulos.dto.Tasasijasaanto;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatapajonoTila;
import fi.vm.sade.sijoittelu.tulos.dto.comparator.HakemusDTOComparator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SijoitteluTulosConverterImpl implements SijoitteluTulosConverter {
    private HakemusDTOComparator hakemusDTOComparator = new HakemusDTOComparator();

    @Override
    public List<HakukohdeDTO> convert(List<Hakukohde> hakukohdeList) {
        return hakukohdeList.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public Iterator<HakukohdeDTO> convert(Iterator<Hakukohde> hakukohteet) {
        return new Iterator<HakukohdeDTO>() {
            @Override
            public boolean hasNext() {
                return hakukohteet.hasNext();
            }

            @Override
            public HakukohdeDTO next() {
                return convert(hakukohteet.next());
            }
        };
    }

    @Override
    public HakukohdeDTO convert(Hakukohde hakukohde) {
        if (hakukohde == null) {
            return null;
        }
        HakukohdeDTO dto = new HakukohdeDTO();
        BigDecimal ensikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet = Optional.ofNullable(hakukohde.getHakijaryhmat()).orElse(Collections.emptyList()).stream().findFirst().map(Hakijaryhma::getAlinHyvaksyttyPistemaara).orElse(null);
        dto.setEnsikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet(ensikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet);
        dto.setOid(hakukohde.getOid());
        dto.setSijoitteluajoId(hakukohde.getSijoitteluajoId());
        dto.setTarjoajaOid(hakukohde.getTarjoajaOid());
        dto.setTila(EnumConverter.convert(HakukohdeTila.class, hakukohde.getTila()));
        dto.setKaikkiJonotSijoiteltu(hakukohde.isKaikkiJonotSijoiteltu());
        for (Valintatapajono valintatapajono : hakukohde.getValintatapajonot()) {
            dto.getValintatapajonot().add(convert(valintatapajono, hakukohde));
        }
        for (Hakijaryhma hakijaryhma : hakukohde.getHakijaryhmat()) {
            dto.getHakijaryhmat().add(convert(hakijaryhma));
        }
        return dto;
    }

    private ValintatapajonoDTO convert(Valintatapajono valintatapajono,
                                       Hakukohde hakukohde) {
        ValintatapajonoDTO dto = new ValintatapajonoDTO();
        dto.setHakeneet(countHakeneet(valintatapajono));
        dto.setAloituspaikat(valintatapajono.getAloituspaikat());
        dto.setAlkuperaisetAloituspaikat(valintatapajono.getAlkuperaisetAloituspaikat());
        dto.setEiVarasijatayttoa(valintatapajono.getEiVarasijatayttoa());
        dto.setKaikkiEhdonTayttavatHyvaksytaan(valintatapajono.getKaikkiEhdonTayttavatHyvaksytaan());
        dto.setPoissaOlevaTaytto(valintatapajono.getPoissaOlevaTaytto());
        dto.setValintaesitysHyvaksytty(valintatapajono.getValintaesitysHyvaksytty());
        dto.setOid(valintatapajono.getOid());
        dto.setNimi(valintatapajono.getNimi());
        dto.setPrioriteetti(valintatapajono.getPrioriteetti());
        dto.setTasasijasaanto(EnumConverter.convert(Tasasijasaanto.class, valintatapajono.getTasasijasaanto()));
        dto.setTila(EnumConverter.convert(ValintatapajonoTila.class, valintatapajono.getTila()));
        dto.setTayttojono(valintatapajono.getTayttojono());
        dto.setVarasijat(valintatapajono.getVarasijat());
        dto.setVarasijaTayttoPaivat(valintatapajono.getVarasijaTayttoPaivat());
        dto.setVarasijojaKaytetaanAlkaen(valintatapajono.getVarasijojaKaytetaanAlkaen());
        dto.setVarasijojaTaytetaanAsti(valintatapajono.getVarasijojaTaytetaanAsti());
        for (Hakemus hakemus : valintatapajono.getHakemukset()) {
            dto.getHakemukset().add(convert(hakemus, valintatapajono, hakukohde));
        }
        dto.setAlinHyvaksyttyPistemaara(valintatapajono.getAlinHyvaksyttyPistemaara());
        dto.setHyvaksytty(valintatapajono.getHyvaksytty());
        dto.setVaralla(valintatapajono.getVaralla());
        sortHakemukset(dto);
        return dto;
    }

    private Integer countHakeneet(Valintatapajono valintatapajono) {
        List<Hakemus> hakemuses = Optional.ofNullable(valintatapajono.getHakemukset()).orElse(Collections.emptyList());
        return Optional.ofNullable(valintatapajono.getHakemustenMaara()).orElse(hakemuses.size());
    }

    private HakemusDTO convert(Hakemus ha, Valintatapajono valintatapajono, Hakukohde hakukohde) {
        HakemusDTO dto = new HakemusDTO();
        dto.setOnkoMuuttunutViimeSijoittelussa(ha.isOnkoMuuttunutViimeSijoittelussa());
        dto.setEtunimi(ha.getEtunimi());
        dto.setHakemusOid(ha.getHakemusOid());
        dto.setHakijaOid(ha.getHakijaOid());
        dto.setHyvaksyttyHarkinnanvaraisesti(ha.isHyvaksyttyHarkinnanvaraisesti());
        dto.setJonosija(ha.getJonosija());
        dto.setPrioriteetti(ha.getPrioriteetti());
        dto.setSukunimi(ha.getSukunimi());
        dto.setSiirtynytToisestaValintatapajonosta(ha.getSiirtynytToisestaValintatapajonosta());
        dto.setTasasijaJonosija(ha.getTasasijaJonosija());
        dto.setTila(EnumConverter.convert(HakemuksenTila.class, ha.getTila()));
        dto.setTilanKuvaukset(ha.getTilanKuvaukset());
        applyTilaHistoria(ha, dto);
        dto.setPisteet(ha.getPisteet());
        if (hakukohde != null) {
            dto.setTarjoajaOid(hakukohde.getTarjoajaOid());
            dto.setHakukohdeOid(hakukohde.getOid());
            dto.setSijoitteluajoId(hakukohde.getSijoitteluajoId());
        }
        if (valintatapajono != null) {
            dto.setValintatapajonoOid(valintatapajono.getOid());
        }
        dto.setVarasijanNumero(ha.getVarasijanNumero());
        dto.setHyvaksyttyHakijaryhmista(ha.getHyvaksyttyHakijaryhmista());
        return dto;
    }

    private HakijaryhmaDTO convert(Hakijaryhma hakijaryhma) {
        HakijaryhmaDTO dto = new HakijaryhmaDTO();
        dto.setPrioriteetti(hakijaryhma.getPrioriteetti());
        dto.setPaikat(hakijaryhma.getPaikat());
        dto.setOid(hakijaryhma.getOid());
        dto.setNimi(hakijaryhma.getNimi());
        dto.setHakukohdeOid(hakijaryhma.getHakukohdeOid());
        dto.setKiintio(hakijaryhma.getKiintio());
        dto.setKaytaKaikki(hakijaryhma.isKaytaKaikki());
        dto.setTarkkaKiintio(hakijaryhma.isTarkkaKiintio());
        dto.setKaytetaanRyhmaanKuuluvia(hakijaryhma.isKaytetaanRyhmaanKuuluvia());
        dto.setValintatapajonoOid(hakijaryhma.getValintatapajonoOid());
        dto.setHakemusOid(hakijaryhma.getHakemusOid());
        dto.setHakijaryhmatyyppikoodiUri(hakijaryhma.getHakijaryhmatyyppikoodiUri());
        return dto;
    }

    private void applyTilaHistoria(Hakemus ha, HakemusDTO dto) {
        for (TilaHistoria tilaHistoria : ha.getTilaHistoria()) {
            TilaHistoriaDTO thDTO = new TilaHistoriaDTO();
            thDTO.setLuotu(tilaHistoria.getLuotu());
            thDTO.setTila(tilaHistoria.getTila().name());
            dto.getTilaHistoria().add(thDTO);
        }
    }

    @Override
    public SijoitteluajoDTO convert(SijoitteluAjo ajo) {
        SijoitteluajoDTO dto = new SijoitteluajoDTO();
        dto.setEndMils(ajo.getEndMils());
        dto.setHakuOid(ajo.getHakuOid());
        dto.setSijoitteluajoId(ajo.getSijoitteluajoId());
        dto.setStartMils(ajo.getStartMils());
        if (ajo.getHakukohteet() == null) {
            return dto;
        }
        for (HakukohdeItem hki : ajo.getHakukohteet().stream().filter(Objects::nonNull).collect(Collectors.toList())) {
            HakukohdeDTO hdto = new HakukohdeDTO();
            hdto.setOid(hki.getOid());
            dto.getHakukohteet().add(hdto);
        }
        return dto;
    }

    @Override
    public void sortHakemukset(ValintatapajonoDTO valintatapajonoDTO) {
        Collections.sort(valintatapajonoDTO.getHakemukset(), hakemusDTOComparator);
    }

}
