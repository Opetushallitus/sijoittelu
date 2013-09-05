package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.dto.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 5.9.2013
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
@Component
public class SijoitteluTulosConverterImpl implements SijoitteluTulosConverter {

    @Override
    public List<HakemusDTO> extractHakemukset(List<Hakukohde> hakukohdeList, String hakemusOid) {
        List<HakemusDTO> hakemusDTOList = new ArrayList<HakemusDTO>();

        for(Hakukohde h : hakukohdeList) {
            for(Valintatapajono v : h.getValintatapajonot()) {
                for(Hakemus ha : v.getHakemukset()) {
                    if(hakemusOid.equals(ha.getHakemusOid())) {
                        HakemusDTO dto = convert(ha);
                        dto.setTarjoajaOid(h.getTarjoajaOid());
                        dto.setValintatapajonoOid(v.getOid());
                        dto.setHakukohdeOid(h.getOid());
                        dto.setSijoitteluajoId(h.getSijoitteluajoId());
                        hakemusDTOList.add(dto);
                    }
                }
            }
        }
        return hakemusDTOList;
    }

    @Override
    public HakukohdeDTO convert(Hakukohde hakukohde) {
        HakukohdeDTO dto = new HakukohdeDTO();
        dto.setOid(hakukohde.getOid());
        dto.setSijoitteluajoId(hakukohde.getSijoitteluajoId());
        dto.setTarjoajaOid(hakukohde.getTarjoajaOid());
        dto.setTila(hakukohde.getTila());
        for(Valintatapajono valintatapajono : hakukohde.getValintatapajonot()) {
            dto.getValintatapajonot().add(convert(valintatapajono));
        }
        return dto;
    }

    private ValintatapajonoDTO convert(Valintatapajono valintatapajono) {
        ValintatapajonoDTO dto = new ValintatapajonoDTO();
        dto.setAloituspaikat(valintatapajono.getAloituspaikat());
        dto.setEiVarasijatayttoa(valintatapajono.getEiVarasijatayttoa());
        dto.setOid(valintatapajono.getOid());
        dto.setPrioriteetti(valintatapajono.getPrioriteetti());
        dto.setTasasijasaanto(valintatapajono.getTasasijasaanto());
        dto.setTila(valintatapajono.getTila());
        for(Hakemus hakemus : valintatapajono.getHakemukset()) {
            dto.getHakemukset().add(convert(hakemus));
        }
        return dto;
    }

    private HakemusDTO convert(Hakemus ha) {
        HakemusDTO dto = new HakemusDTO();
        dto.setEtunimi(ha.getEtunimi());
        dto.setHakemusOid(ha.getHakemusOid());
        dto.setHakijaOid(ha.getHakijaOid());
        //dto.setHakuOid(h.get);//add to domain later
        dto.setJonosija(ha.getJonosija());
        dto.setPrioriteetti(ha.getPrioriteetti());
        dto.setSukunimi(ha.getSukunimi());
        dto.setTasasijaJonosija(ha.getTasasijaJonosija());
        dto.setTila(ha.getTila());
        return dto;
    }

    @Override
    public SijoitteluajoDTO convert(SijoitteluAjo ajo) {
        SijoitteluajoDTO dto = new SijoitteluajoDTO();
        dto.setEndMils(ajo.getEndMils());
        dto.setHakuOid(ajo.getHakuOid());
        dto.setSijoitteluajoId(ajo.getSijoitteluajoId());
        dto.setStartMils(ajo.getStartMils());
        for(HakukohdeItem hki : ajo.getHakukohteet())  {
            HakukohdeDTO hdto = new HakukohdeDTO();
            hdto.setOid(hki.getOid());
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
         for(SijoitteluAjo sa : s.getSijoitteluajot()) {
             SijoitteluajoDTO sdto = new SijoitteluajoDTO();
             sdto.setEndMils(sa.getEndMils());
             sdto.setSijoitteluajoId(sa.getSijoitteluajoId());
             sdto.setStartMils(sa.getStartMils());
             dto.getSijoitteluajot().add(sdto);
         }
        return dto;
    }
}
