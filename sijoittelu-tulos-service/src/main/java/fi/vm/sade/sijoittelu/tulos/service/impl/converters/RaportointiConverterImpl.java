package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.HakutoiveDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 17.9.2013
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
@Component
public class RaportointiConverterImpl implements RaportointiConverter {
    @Override
    public List<HakijaDTO> convert(List<Hakukohde> hakukohteet) {
        HashMap<String, HakijaDTO> hakijat = new HashMap<String, HakijaDTO>();
        for(Hakukohde hakukohde : hakukohteet) {
            for(Valintatapajono valintatapajono : hakukohde.getValintatapajonot()) {
                for(Hakemus hakemus : valintatapajono.getHakemukset()) {
                    HakijaDTO hakijaRaportointiDTO =  getOrCreateHakijaRaportointiDTO(hakijat, hakemus);
                    HakutoiveDTO raportointiHakutoiveDTO = new HakutoiveDTO();
                    raportointiHakutoiveDTO.setHakukohdeOid(hakukohde.getOid());
                    raportointiHakutoiveDTO.setHakutoive(hakemus.getPrioriteetti());
                    raportointiHakutoiveDTO.setJonosija(hakemus.getJonosija());


                    raportointiHakutoiveDTO.setHyvaksyttyHarkinnanvaraisesti(hakemus.isHyvaksyttyHarkinnanvaraisesti());
                    //raportointiHakutoiveDTO.setPaasyJaSoveltuvuusKokeenTulos(hakemus.);
                    raportointiHakutoiveDTO.setPisteet(hakemus.getPisteet());
                    raportointiHakutoiveDTO.setTasasijaJonosija(hakemus.getTasasijaJonosija());
                    raportointiHakutoiveDTO.setTila(EnumConverter.convert(HakemuksenTila.class, hakemus.getTila()));



                    hakijaRaportointiDTO.getHakutoiveet().add(raportointiHakutoiveDTO);

                }
            }
        }
        return new ArrayList<HakijaDTO>(hakijat.values());
    }

    private HakijaDTO getOrCreateHakijaRaportointiDTO(HashMap<String, HakijaDTO> hakijat, Hakemus hakemus) {
        HakijaDTO hakijaRaportointiDTO = hakijat.get(hakemus.getHakemusOid());
        if(hakijaRaportointiDTO == null) {
            hakijaRaportointiDTO = new HakijaDTO();
            hakijaRaportointiDTO.setEtunimi(hakemus.getEtunimi());
            hakijaRaportointiDTO.setSukunimi(hakemus.getSukunimi());
            hakijaRaportointiDTO.setHakemusOid(hakemus.getHakemusOid());
        }
        hakijat.put(hakemus.getHakemusOid(), hakijaRaportointiDTO);
        return hakijaRaportointiDTO;
    }


}
