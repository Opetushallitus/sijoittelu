package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.HakemusDTO;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeTila;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.Tasasijasaanto;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatapajonoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatapajonoTila;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 5.9.2013 Time: 14:28 To
 * change this template use File | Settings | File Templates.
 */
@Component
public class SijoitteluTulosConverterImpl implements SijoitteluTulosConverter {

    @Override
    public List<HakukohdeDTO> convert(List<Hakukohde> hakukohdeList) {
        List<HakukohdeDTO> hakemusDTOList = new ArrayList<HakukohdeDTO>();
        for (Hakukohde h : hakukohdeList) {
            hakemusDTOList.add(convert(h));
        }
        return hakemusDTOList;
    }

    @Override
    public HakukohdeDTO convert(Hakukohde hakukohde) {
        if (hakukohde == null) {
            return null;
        }
        HakukohdeDTO dto = new HakukohdeDTO();
        dto.setOid(hakukohde.getOid());
        dto.setSijoitteluajoId(hakukohde.getSijoitteluajoId());
        dto.setTarjoajaOid(hakukohde.getTarjoajaOid());
        dto.setTila(EnumConverter.convert(HakukohdeTila.class, hakukohde.getTila()));
        for (Valintatapajono valintatapajono : hakukohde.getValintatapajonot()) {
            dto.getValintatapajonot().add(convert(valintatapajono, hakukohde));
        }
        return dto;
    }

    private ValintatapajonoDTO convert(Valintatapajono valintatapajono, Hakukohde hakukohde) {
        ValintatapajonoDTO dto = new ValintatapajonoDTO();
        dto.setAloituspaikat(valintatapajono.getAloituspaikat());
        dto.setEiVarasijatayttoa(valintatapajono.getEiVarasijatayttoa());
        dto.setOid(valintatapajono.getOid());
        dto.setPrioriteetti(valintatapajono.getPrioriteetti());
        dto.setTasasijasaanto(EnumConverter.convert(Tasasijasaanto.class, valintatapajono.getTasasijasaanto()));
        dto.setTila(EnumConverter.convert(ValintatapajonoTila.class, valintatapajono.getTila()));
        for (Hakemus hakemus : valintatapajono.getHakemukset()) {
            dto.getHakemukset().add(convert(hakemus, valintatapajono, hakukohde));
        }
        return dto;
    }

    private HakemusDTO convert(Hakemus ha, Valintatapajono valintatapajono, Hakukohde hakukohde) {
        HakemusDTO dto = new HakemusDTO();
        dto.setEtunimi(ha.getEtunimi());
        dto.setHakemusOid(ha.getHakemusOid());
        dto.setHakijaOid(ha.getHakijaOid());
        dto.setHyvaksyttyHarkinnanvaraisesti(ha.isHyvaksyttyHarkinnanvaraisesti());
        // dto.setHakuOid(h.get);//add to domain later
        dto.setJonosija(ha.getJonosija());
        dto.setPrioriteetti(ha.getPrioriteetti());
        dto.setSukunimi(ha.getSukunimi());
        dto.setTasasijaJonosija(ha.getTasasijaJonosija());
        dto.setTila(EnumConverter.convert(HakemuksenTila.class, ha.getTila()));
        dto.setPisteet(ha.getPisteet());
        if (hakukohde != null) {
            dto.setTarjoajaOid(hakukohde.getTarjoajaOid());
            dto.setHakukohdeOid(hakukohde.getOid());
            dto.setSijoitteluajoId(hakukohde.getSijoitteluajoId());
        }
        if (valintatapajono != null) {
            dto.setValintatapajonoOid(valintatapajono.getOid());
        }
        return dto;
    }

    @Override
    public SijoitteluajoDTO convert(SijoitteluAjo ajo) {
        SijoitteluajoDTO dto = new SijoitteluajoDTO();
        dto.setEndMils(ajo.getEndMils());
        dto.setHakuOid(ajo.getHakuOid());
        dto.setSijoitteluajoId(ajo.getSijoitteluajoId());
        dto.setStartMils(ajo.getStartMils());
        for (HakukohdeItem hki : ajo.getHakukohteet()) {
            HakukohdeDTO hdto = new HakukohdeDTO();
            hdto.setOid(hki.getOid());
            dto.getHakukohteet().add(hdto);
        }
        return dto;
    }

    @Override
    public SijoitteluDTO convert(Sijoittelu s) {
        SijoitteluDTO dto = new SijoitteluDTO();
        dto.setCreated(s.getCreated());
        dto.setHakuOid(s.getHakuOid());
        dto.setSijoittele(s.isSijoittele());
        dto.setSijoitteluId(s.getSijoitteluId());
        for (SijoitteluAjo sa : s.getSijoitteluajot()) {
            SijoitteluajoDTO sdto = new SijoitteluajoDTO();
            sdto.setEndMils(sa.getEndMils());
            sdto.setSijoitteluajoId(sa.getSijoitteluajoId());
            sdto.setStartMils(sa.getStartMils());
            dto.getSijoitteluajot().add(sdto);
        }
        return dto;
    }
}
