package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import java.util.*;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.HakemusDTO;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.tulos.dto.PistetietoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatapajonoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveenValintatapajonoDTO;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 17.9.2013 Time: 14:49 To
 * change this template use File | Settings | File Templates.
 */
@Component
public class RaportointiConverterImpl implements RaportointiConverter {

	@Override
	public List<HakijaDTO> convert(List<HakukohdeDTO> hakukohteet) {
		HashMap<String, HakijaDTO> hakijat = new HashMap<String, HakijaDTO>();
		for (HakukohdeDTO hakukohde : hakukohteet) {
			for (ValintatapajonoDTO valintatapajono : hakukohde
					.getValintatapajonot()) {
				for (HakemusDTO hakemusDTO : valintatapajono.getHakemukset()) {
					HakijaDTO hakijaRaportointiDTO = getOrCreateHakijaRaportointiDTO(
							hakijat, hakemusDTO);
					HakutoiveDTO raportointiHakutoiveDTO = getOrCreateHakutoive(
							hakijaRaportointiDTO, hakemusDTO, hakukohde);
					HakutoiveenValintatapajonoDTO hakutoiveenValintatapajonoDTO = new HakutoiveenValintatapajonoDTO();
					raportointiHakutoiveDTO.getHakutoiveenValintatapajonot()
							.add(hakutoiveenValintatapajonoDTO);

					hakutoiveenValintatapajonoDTO.setHakeneet(valintatapajono
							.getHakeneet());
					hakutoiveenValintatapajonoDTO
							.setAlinHyvaksyttyPistemaara(valintatapajono
									.getAlinHyvaksyttyPistemaara());
					hakutoiveenValintatapajonoDTO.setHyvaksytty(valintatapajono
							.getHyvaksytty());
					hakutoiveenValintatapajonoDTO.setVaralla(valintatapajono
							.getVaralla());
					hakutoiveenValintatapajonoDTO.setHakeneet(valintatapajono
							.getHakeneet());
					hakutoiveenValintatapajonoDTO
							.setValintatapajonoOid(valintatapajono.getOid());
                    hakutoiveenValintatapajonoDTO.setTayttojono(valintatapajono.getTayttojono());
                    hakutoiveenValintatapajonoDTO.setVarasijat(valintatapajono.getVarasijat());
                    hakutoiveenValintatapajonoDTO.setVarasijaTayttoPaivat(valintatapajono.getVarasijaTayttoPaivat());
                    hakutoiveenValintatapajonoDTO.setVarasijojaKaytetaanAlkaen(valintatapajono.getVarasijojaKaytetaanAlkaen());
                    hakutoiveenValintatapajonoDTO.setVarasijojaTaytetaanAsti(valintatapajono.getVarasijojaTaytetaanAsti());
					hakutoiveenValintatapajonoDTO
							.setValintatapajonoNimi(valintatapajono.getNimi());

					hakutoiveenValintatapajonoDTO.setVarasijanNumero(hakemusDTO
							.getVarasijanNumero());
					hakutoiveenValintatapajonoDTO
							.setTasasijaJonosija(hakemusDTO
									.getTasasijaJonosija());
					hakutoiveenValintatapajonoDTO.setPisteet(hakemusDTO
							.getPisteet());
					hakutoiveenValintatapajonoDTO.setJonosija(hakemusDTO
							.getJonosija());
					hakutoiveenValintatapajonoDTO
							.setTila(EnumConverter.convert(
									HakemuksenTila.class, hakemusDTO.getTila()));
					hakutoiveenValintatapajonoDTO.setTilanKuvaukset(hakemusDTO
							.getTilanKuvaukset());
					hakutoiveenValintatapajonoDTO
							.setHyvaksyttyHarkinnanvaraisesti(hakemusDTO
									.isHyvaksyttyHarkinnanvaraisesti());
					hakutoiveenValintatapajonoDTO
							.setPaasyJaSoveltuvuusKokeenTulos(hakemusDTO
									.getPaasyJaSoveltuvuusKokeenTulos());
					hakutoiveenValintatapajonoDTO
							.setValintatapajonoPrioriteetti(hakemusDTO
									.getPrioriteetti());

					// hakutoiveenValintatapajonoDTO.setValintatapajonoPrioriteetti(hakemusDTO.getPrioriteetti());
					hakutoiveenValintatapajonoDTO
							.setValintatapajonoOid(valintatapajono.getOid());

					applyPistetiedot(raportointiHakutoiveDTO,
							hakemusDTO.getPistetiedot());
				}
			}
		}
		return new ArrayList<HakijaDTO>(hakijat.values());
	}

	private Map<String, HakukohdeDTO> mapHakukohteet(
			List<HakukohdeDTO> hakukohteet) {
		Map<String, HakukohdeDTO> tmp = Maps.newHashMap();
		for (HakukohdeDTO h : hakukohteet) {
			tmp.put(h.getOid(), h);
		}
		return tmp;
	}

	private ValintatapajonoDTO getValintatapajono(HakukohdeDTO hakukohde,
			String jonoOid) {
		for (ValintatapajonoDTO j : hakukohde.getValintatapajonot()) {
			if (jonoOid.equals(j.getOid())) {
				return j;
			}
		}
		return null;
	}

	@Override
	public List<HakijaDTO> convert(List<HakukohdeDTO> hakukohteet,
			List<Valintatulos> kaikkienValintatulokset) {
		// convert hakijat
		List<HakijaDTO> hakijat = convert(hakukohteet);
		// apply valintatulos
		Map<String, HakukohdeDTO> hakukohteetMap = mapHakukohteet(hakukohteet);
        Map<String, List<Valintatulos>> valintatulosMap = mapValintatulokset(kaikkienValintatulokset);
		for (HakijaDTO hakija : hakijat) {
            List<Valintatulos> valintatulokset = valintatulosMap.get(hakija
					.getHakemusOid());
			if (valintatulokset != null && !valintatulokset.isEmpty()) {
				for (HakutoiveDTO hakutoiveDTO : hakija.getHakutoiveet()) {
					HakukohdeDTO hakukohde = hakukohteetMap.get(hakutoiveDTO
							.getHakukohdeOid());
					for (HakutoiveenValintatapajonoDTO valintatapajonoDTO : hakutoiveDTO
							.getHakutoiveenValintatapajonot()) {
                        if (valintatapajonoDTO == null) {
                            continue;
                        }
                        for (Valintatulos valintatulos : valintatulokset) {
                            if (valintatulos == null) {
                                continue;
                            }
                            if (valintatulos.getValintatapajonoOid().equals(valintatapajonoDTO.getValintatapajonoOid())) {

                                valintatapajonoDTO.setVastaanottotieto(EnumConverter.convert(ValintatuloksenTila.class, valintatulos.getTila()));
                                valintatapajonoDTO.setJulkaistavissa(valintatulos.getJulkaistavissa());
                                valintatapajonoDTO.setHyvaksyttyVarasijalta(valintatulos.getHyvaksyttyVarasijalta());
                                valintatapajonoDTO.setVastaanottotilanViimeisinMuutos(viimeisinVastaanottotilanMuutos(valintatulos));

                                if (ValintatuloksenTila.VASTAANOTTANUT.equals(valintatapajonoDTO.getVastaanottotieto()) || ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT.equals(valintatapajonoDTO.getVastaanottotieto())) {
                                    valintatapajonoDTO.setIlmoittautumisTila(EnumConverter.convert(IlmoittautumisTila.class, valintatulos.getIlmoittautumisTila()));
                                }
                            }
                        }
					}
				}
			}
		}
		return hakijat;
	}

    private static Date viimeisinVastaanottotilanMuutos(Valintatulos valintatulos) {
        if(valintatulos != null && !valintatulos.getLogEntries().isEmpty()) {
            return valintatulos.getLogEntries().get(valintatulos.getLogEntries().size() - 1).getLuotu();
        }
        return null;
    }

    private Map<String, List<Valintatulos>> mapValintatulokset(
			List<Valintatulos> valintatulokset) {
        Map<String, List<Valintatulos>> map = new HashMap<String, List<Valintatulos>>();
		for (Valintatulos valintatulos : valintatulokset) {
            if (map.containsKey(valintatulos.getHakemusOid())) {
                map.get(valintatulos.getHakemusOid()).add(valintatulos);
            } else {
                List<Valintatulos> v = Lists.newArrayListWithExpectedSize(2);
                v.add(valintatulos);
                map.put(valintatulos.getHakemusOid(), v);
            }
		}
		return map;
	}

	private void applyPistetiedot(HakutoiveDTO dto,
			List<PistetietoDTO> pistetiedot) {
		for (PistetietoDTO pistetieto : pistetiedot) {
			if (pistetieto.getTunniste() != null) {
				PistetietoDTO pt = null;
				for (PistetietoDTO lpto : dto.getPistetiedot()) {
					if (pistetieto.getTunniste().equals(lpto.getTunniste())) {
						pt = lpto;
						continue;
					}
				}
				if (pt == null) {
					pt = new PistetietoDTO();
					dto.getPistetiedot().add(pt);
				}
				pt.setArvo(pistetieto.getArvo());
				pt.setLaskennallinenArvo(pistetieto.getLaskennallinenArvo());
				pt.setOsallistuminen(pistetieto.getOsallistuminen());
				pt.setTunniste(pistetieto.getTunniste());

			}
		}
	}

	private HakutoiveDTO getOrCreateHakutoive(HakijaDTO hakijaDTO,
			HakemusDTO hakemusDTO, HakukohdeDTO hakukohde) {
		HakutoiveDTO hakutoiveDTO = null;
		for (HakutoiveDTO hd : hakijaDTO.getHakutoiveet()) {
			if (hd.getHakukohdeOid().equals(hakemusDTO.getHakukohdeOid())) {
				hakutoiveDTO = hd;
			}
		}
		if (hakutoiveDTO == null) {
			hakutoiveDTO = new HakutoiveDTO();
			hakutoiveDTO.setTarjoajaOid(hakemusDTO.getTarjoajaOid());
			hakutoiveDTO.setHakukohdeOid(hakemusDTO.getHakukohdeOid());
            hakutoiveDTO.setKaikkiJonotSijoiteltu(hakukohde.isKaikkiJonotSijoiteltu());
			hakutoiveDTO.setHakutoive(hakemusDTO.getPrioriteetti());
			hakijaDTO.getHakutoiveet().add(hakutoiveDTO);
		}
		return hakutoiveDTO;

	}

	private HakijaDTO getOrCreateHakijaRaportointiDTO(
			HashMap<String, HakijaDTO> hakijat, HakemusDTO hakemus) {
		HakijaDTO hakijaRaportointiDTO = hakijat.get(hakemus.getHakemusOid());
		if (hakijaRaportointiDTO == null) {
			hakijaRaportointiDTO = new HakijaDTO();
			hakijaRaportointiDTO.setEtunimi(hakemus.getEtunimi());
			hakijaRaportointiDTO.setSukunimi(hakemus.getSukunimi());
            hakijaRaportointiDTO.setHakijaOid(hakemus.getHakijaOid());
			hakijaRaportointiDTO.setHakemusOid(hakemus.getHakemusOid());
            hakijat.put(hakemus.getHakemusOid(), hakijaRaportointiDTO);
        }
		return hakijaRaportointiDTO;
	}

}
