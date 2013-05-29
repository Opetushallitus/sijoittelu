package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HenkiloWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class SijoitteluAlgorithmImpl implements SijoitteluAlgorithm {

	protected SijoitteluAlgorithmImpl() {
	}

	protected SijoitteluajoWrapper sijoitteluAjo;

	// protected Map<String, Tasasijasaanto> tasasijasaannot;

	protected List<PreSijoitteluProcessor> preSijoitteluProcessors;

	protected List<PostSijoitteluProcessor> postSijoitteluProcessors;

	protected int depth = 0;

	@Override
	public void start() {
		runPreProcessors();
		sijoittele();
		runPostProcessors();
	}

	private void runPostProcessors() {
		for (PostSijoitteluProcessor processor : postSijoitteluProcessors) {
			processor.process(sijoitteluAjo);
		}
	}

	private void runPreProcessors() {
		for (PreSijoitteluProcessor processor : preSijoitteluProcessors) {
			processor.process(sijoitteluAjo);
		}
	}

	private void sijoittele() {
		// System.out.println(PrintHelper.tulostaSijoittelu(this));
		for (HakukohdeWrapper hakukohde : sijoitteluAjo.getHakukohteet()) {
			sijoittele(hakukohde, 0);
		}
	}

	private void sijoittele(HakukohdeWrapper hakukohde, int n) {
		n++;
		if (n > depth) {
			depth = n;
		}
		// System.out.println("Depth = " + n);
		for (ValintatapajonoWrapper valintatapajono : hakukohde.getValintatapajonot()) {
			this.sijoittele(valintatapajono, n);
		}
		for (HakijaryhmaWrapper hakijaryhmaWrapper : hakukohde.getHakijaryhmaWrappers()) {
			this.sijoittele(hakijaryhmaWrapper, n);
		}
	}

	private void sijoittele(ValintatapajonoWrapper valintatapajono, int n) {

		// System.out.println("====Sijoittele: " +
		// valintatapajono.getValintatapajono().getOid());

		ArrayList<HakemusWrapper> hyvaksyttavaksi = new ArrayList<HakemusWrapper>();
		ArrayList<HakemusWrapper> varalle = new ArrayList<HakemusWrapper>();

		Tasasijasaanto saanto = valintatapajono.getValintatapajono().getTasasijasaanto();

		ArrayList<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset = eiKorvattavissaOlevatHyvaksytytHakemukset(valintatapajono);

		// for (HakemusWrapper w : eiKorvattavissaOlevatHyvaksytytHakemukset) {
		// System.out.print("======EI KORVATTAVISSA" +
		// w.getHenkilo().getHakijaOid());
		// }

		int aloituspaikat = valintatapajono.getValintatapajono().getAloituspaikat();
		int tilaa = aloituspaikat - eiKorvattavissaOlevatHyvaksytytHakemukset.size();
		boolean tasaSijaTilanne = false;
		boolean tasasijaTilanneRatkaistu = false;

		ListIterator<HakemusWrapper> it = valintatapajono.getHakemukset().listIterator();
		while (it.hasNext()) {
			ArrayList<HakemusWrapper> kaikkiTasasijaHakemukset = tasasijaHakemukset(it);
			ArrayList<HakemusWrapper> valituksiHaluavatHakemukset = valituksiHaluavatHakemukset(kaikkiTasasijaHakemukset);

			if (tilaa - valituksiHaluavatHakemukset.size() <= 0) {
				// vain ylitaytto tai alitaytto vaatii erillista
				// tasasijanhallintaa
				if (saanto == Tasasijasaanto.YLITAYTTO || saanto == Tasasijasaanto.ALITAYTTO) {
					tasaSijaTilanne = true;
				}
			}
			for (HakemusWrapper hk : kaikkiTasasijaHakemukset) {
				if (eiKorvattavissaOlevatHyvaksytytHakemukset.contains(hk)) {
					// ei voida tehdä mitaan, tila on jo poistettu. ignoretetaan
				} else if (valituksiHaluavatHakemukset.contains(hk)) {
					if (tasaSijaTilanne && !tasasijaTilanneRatkaistu) {
						if (saanto == Tasasijasaanto.ALITAYTTO) {
							varalle.add(hk);
						} else if (saanto == Tasasijasaanto.YLITAYTTO) {
							hyvaksyttavaksi.add(hk);
						}
					} else if (tasasijaTilanneRatkaistu) {
						varalle.add(hk);
					} else if (tilaa > 0) {
						hyvaksyttavaksi.add(hk);
						tilaa--;
					} else {
						varalle.add(hk);
					}
				} else {
					varalle.add(hk);
				}
			}
			if (tasaSijaTilanne) {
				tasasijaTilanneRatkaistu = true;
			}

		}

		// kay lavitse muutokset ja tarkista tarvitaanko rekursiota?
		ArrayList<HakemusWrapper> muuttuneetHakemukset = new ArrayList<HakemusWrapper>();
		for (HakemusWrapper hakemusWrapper : hyvaksyttavaksi) {
			if (hakemusWrapper.getHakemus().getTila() != HakemuksenTila.HYVAKSYTTY) {
				muuttuneetHakemukset.addAll(hyvaksyHakemus(hakemusWrapper));
			}
		}
		for (HakemusWrapper hakemusWrapper : varalle) {
			if (hakemusWrapper.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY) {
				muuttuneetHakemukset.addAll(asetaVaralleHakemus(hakemusWrapper));
			}
		}

		// System.out.println("====DONE");

		for (HakukohdeWrapper v : uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset)) {
			sijoittele(v, n);
		}

	}

	private void sijoittele(HakijaryhmaWrapper hakijaryhmaWrapper, int n) {
		ArrayList<HakemusWrapper> muuttuneetHakemukset = new ArrayList<HakemusWrapper>();
		List<List<HakemusWrapper>> hakijaryhmanVarasijallaOlevat = hakijaryhmanVarasijajarjestys(hakijaryhmaWrapper);
		ListIterator<List<HakemusWrapper>> it = hakijaryhmanVarasijallaOlevat.listIterator();

		while (it.hasNext() && hakijaryhmassaVajaata(hakijaryhmaWrapper) > 0) {
			List<HakemusWrapper> tasasijaVarasijaHakemukset = it.next();
			for (HakemusWrapper paras : tasasijaVarasijaHakemukset) {
				ValintatapajonoWrapper v = paras.getValintatapajono();
				HakemusWrapper huonoin = haeHuonoinValittuEiVajaaseenRyhmaanKuuluva(v);
				muuttuneetHakemukset.addAll(hyvaksyHakemus(paras));
				muuttuneetHakemukset.add(paras);
				if (huonoin != null) {
					muuttuneetHakemukset.addAll(asetaVaralleHakemus(huonoin));
				}
			}
		}
		HashSet<HakukohdeWrapper> uudelleenSijoiteltavatHakukohteet = uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset);
		for (HakukohdeWrapper h : uudelleenSijoiteltavatHakukohteet) {
			sijoittele(h, n);
		}

	}

	// private ArrayList<HakemusWrapper>
	// eiValituksiHaluavatHakemukset(ArrayList<HakemusWrapper>
	// kaikkiTasasijaHakemukset, ArrayList<HakemusWrapper>
	// valituksiHaluavatHakemukset) {
	// ArrayList<HakemusWrapper> tasasijaHakemukset = new
	// ArrayList<HakemusWrapper>();
	// for (HakemusWrapper h : kaikkiTasasijaHakemukset) {
	// if (!valituksiHaluavatHakemukset.contains(h)) {
	// tasasijaHakemukset.add(h);
	// }
	// }
	// return tasasijaHakemukset;
	// }

	private ArrayList<HakemusWrapper> valituksiHaluavatHakemukset(ArrayList<HakemusWrapper> hakemukset) {
		ArrayList<HakemusWrapper> tasasijaHakemukset = new ArrayList<HakemusWrapper>();
		for (HakemusWrapper hakemusWrapper : hakemukset) {
			if (hakijaHaluaa(hakemusWrapper) && saannotSallii(hakemusWrapper)) {
				tasasijaHakemukset.add(hakemusWrapper);
			}
		}
		return tasasijaHakemukset;
	}

	// hae hakemukset jotka ovat seuraavana samalla sijalla
	private ArrayList<HakemusWrapper> tasasijaHakemukset(ListIterator<HakemusWrapper> it) {
		ArrayList<HakemusWrapper> tasasijaHakemukset = new ArrayList<HakemusWrapper>();
		while (it.hasNext()) {
			HakemusWrapper h = it.next();
			tasasijaHakemukset.add(h);
			HakemusWrapper seuraava = SijoitteluHelper.peek(it);
			if (seuraava == null || seuraava.getHakemus().getJonosija() != h.getHakemus().getJonosija()) {
				break;
			}
		}
		return tasasijaHakemukset;
	}

	private List<HakemusWrapper> hakijaRyhmaanKuuluvat(List<HakemusWrapper> hakemus, HakijaryhmaWrapper ryhma) {
		List<HakemusWrapper> kuuluvat = new ArrayList<HakemusWrapper>();
		for (HakemusWrapper hakemusWrapper : hakemus) {
			if (ryhma.getHenkiloWrappers().contains(hakemusWrapper.getHenkilo())) {
				kuuluvat.add(hakemusWrapper);
			}
		}
		return kuuluvat;
	}

	private List<List<HakemusWrapper>> hakijaryhmanVarasijajarjestys(HakijaryhmaWrapper hakijaryhmaWrapper) {

		List<List<HakemusWrapper>> list = new ArrayList<List<HakemusWrapper>>();
		List<ListIterator<HakemusWrapper>> iterators = new ArrayList<ListIterator<HakemusWrapper>>();

		for (ValintatapajonoWrapper valintatapajonoWrapper : hakijaryhmaWrapper.getHakukohdeWrapper().getValintatapajonot()) {
			iterators.add(muodostaVarasijaJono(valintatapajonoWrapper.getHakemukset()).listIterator());
		}
		while (true) {
			boolean jatka = false;
			for (ListIterator<HakemusWrapper> it : iterators) {
				if (it.hasNext()) {

					ArrayList<HakemusWrapper> kaikkiTasasijaHakemukset = tasasijaHakemukset(it);
					ArrayList<HakemusWrapper> valituksiHaluavatHakemukset = valituksiHaluavatHakemukset(kaikkiTasasijaHakemukset);

					List<HakemusWrapper> a = hakijaRyhmaanKuuluvat(valituksiHaluavatHakemukset, hakijaryhmaWrapper);
					a = poistaDuplikaatit(a, list);
					if (a != null && !a.isEmpty()) {
						list.add(a);
					}
					jatka = true;
				}
			}
			if (!jatka) {
				break;
			}
		}

		return list;
	}

	private List<HakemusWrapper> muodostaVarasijaJono(List<HakemusWrapper> hakemukset) {
		List<HakemusWrapper> list = new ArrayList<HakemusWrapper>();
		for (HakemusWrapper hakemusWrapper : hakemukset) {
			if (hakemusWrapper.getHakemus().getTila() != HakemuksenTila.HYVAKSYTTY && hakijaHaluaa(hakemusWrapper) && saannotSallii(hakemusWrapper)) {
				list.add(hakemusWrapper);
			}
		}
		return list;
	}

	private List<HakemusWrapper> poistaDuplikaatit(List<HakemusWrapper> hakemukset, List<List<HakemusWrapper>> lista) {
		List<HakemusWrapper> returnable = new ArrayList<HakemusWrapper>();
		for (HakemusWrapper h : hakemukset) {
			boolean found = false;
			for (List<HakemusWrapper> l : lista) {
				for (HakemusWrapper hakemusWrapper : l) {
					if (hakemusWrapper.getHenkilo().equals(h.getHenkilo())) {
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
			if (!found) {
				returnable.add(h);
			}
		}
		return returnable;
	}

	private HashSet<HakukohdeWrapper> uudelleenSijoiteltavatHakukohteet(ArrayList<HakemusWrapper> muuttuneetHakemukset) {
		HashSet<HakukohdeWrapper> hakukohteet = new HashSet<HakukohdeWrapper>();
		for (HakemusWrapper h : muuttuneetHakemukset) {
			hakukohteet.add(h.getValintatapajono().getHakukohdeWrapper());
		}
		return hakukohteet;
	}

	// private HashSet<ValintatapajonoWrapper>
	// uudelleenSijoiteltavatValintatapajonot(ArrayList<HakemusWrapper>
	// muuttuneetHakemukset) {
	// HashSet<ValintatapajonoWrapper> hakukohteet = new
	// HashSet<ValintatapajonoWrapper>();
	// for (HakemusWrapper h : muuttuneetHakemukset) {
	// hakukohteet.add(h.getValintatapajono());
	// }
	// return hakukohteet;
	// }

	// private ArrayList<HakemusWrapper>
	// korvattavissaOlevatHakemukset(ArrayList<HakemusWrapper> hakemukset,
	// ValintatapajonoWrapper valintatapajonoWrapper) {
	// ArrayList<HakemusWrapper> korvattavissaOlevat = new
	// ArrayList<HakemusWrapper>();
	// for (int i = valintatapajonoWrapper.getHakemukset().size() - 1; i >= 0;
	// i--) {
	// HakemusWrapper h = valintatapajonoWrapper.getHakemukset().get(i);
	// if (hakemukset.contains(h)) {
	// break;
	// }
	// if (h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY) {
	// if (voidaanKorvata(h)) {
	// korvattavissaOlevat.add(h);
	// }
	// }
	// }
	// return korvattavissaOlevat;
	// }

	// TODO LISAA TANNE TIETO ILMOITETUISTA YMS.
	private ArrayList<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset(ValintatapajonoWrapper valintatapajono) {
		// System.out.println("ei korvattavissa metodi");
		ArrayList<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset = new ArrayList<HakemusWrapper>();
		for (HakemusWrapper h : valintatapajono.getHakemukset()) {
			if (h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY) {
				if (!voidaanKorvata(h)) {
					eiKorvattavissaOlevatHyvaksytytHakemukset.add(h);
				}
			}
		}
		// System.out.println("yhteensa ei korvattavia" +
		// eiKorvattavissaOlevatHyvaksytytHakemukset.size());
		return eiKorvattavissaOlevatHyvaksytytHakemukset;
	}

	private boolean voidaanKorvata(HakemusWrapper hakemusWrapper) {

		boolean voidaanKorvata = true;

		ArrayList<HakijaryhmaWrapper> hakijanHakijaryhmat = new ArrayList<HakijaryhmaWrapper>();
		for (HakijaryhmaWrapper hakijaryhmaWrapper : hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakijaryhmaWrappers()) {
			if (hakijaryhmaWrapper.getHenkiloWrappers().contains(hakemusWrapper.getHenkilo())) {
				hakijanHakijaryhmat.add(hakijaryhmaWrapper);
			}
		}
		for (HakijaryhmaWrapper a : hakijanHakijaryhmat) {
			if (hakijaryhmassaVajaata(a) >= 0) {
				voidaanKorvata = false;
			}
		}

        //TODO korjaa
		if (!hakemusWrapper.isTilaVoidaanVaihtaa()) {
			voidaanKorvata = false;
		}

		// if (!voidaanKorvata) {
		// System.out.println("ei voida korvata " +
		// hakemusWrapper.getHakemus().getHakijaOid());
		// }

		return voidaanKorvata;
	}

	// private HashSet<HakemusWrapper>
	// asetaVaralleHakemukset(List<HakemusWrapper> tasasijaHakemukset) {
	// HashSet<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new
	// HashSet<HakemusWrapper>();
	// for (HakemusWrapper hakemus : tasasijaHakemukset) {
	// uudelleenSijoiteltavatHakukohteet.addAll(asetaVaralleHakemus(hakemus));
	// }
	// return uudelleenSijoiteltavatHakukohteet;
	// }

	private HashSet<HakemusWrapper> asetaVaralleHakemus(HakemusWrapper varalleAsetettavaHakemusWrapper) {
		// System.out.println("jono" +
		// varalleAsetettavaHakemusWrapper.getValintatapajono().getValintatapajono().getOid()
		// + " aseta varalle hakemus " +
		// varalleAsetettavaHakemusWrapper.getHakemus().getHakijaOid());

		HashSet<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<HakemusWrapper>();
		for (HakemusWrapper hakemusWrapper : varalleAsetettavaHakemusWrapper.getHenkilo().getHakemukset()) {
			if (hakemusWrapper.getHakemus().getTila() != HakemuksenTila.HYLATTY) {
				hakemusWrapper.getHakemus().setTila(HakemuksenTila.VARALLA);
				if (varalleAsetettavaHakemusWrapper.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY) {
					uudelleenSijoiteltavatHakukohteet.add(hakemusWrapper);
				}
			}
		}
		return uudelleenSijoiteltavatHakukohteet;
	}

	// private HashSet<HakemusWrapper> hyvaksyHakemukset(List<HakemusWrapper>
	// tasasijaHakemukset) {
	// HashSet<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new
	// HashSet<HakemusWrapper>();
	// for (HakemusWrapper hakemus : tasasijaHakemukset) {
	// uudelleenSijoiteltavatHakukohteet.addAll(hyvaksyHakemus(hakemus));
	// }
	// return uudelleenSijoiteltavatHakukohteet;
	// }

	private HashSet<HakemusWrapper> hyvaksyHakemus(HakemusWrapper hakemus) {
		// System.out.println("jono" +
		// hakemus.getValintatapajono().getValintatapajono().getOid() +
		// " Hyvaksy hakemus " + hakemus.getHakemus().getHakijaOid());

		HashSet<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<HakemusWrapper>();
		hakemus.getHakemus().setTila(HakemuksenTila.HYVAKSYTTY);
		for (HakemusWrapper h : hakemus.getHenkilo().getHakemukset()) {
			if (h != hakemus && hakemus.getHakemus().getPrioriteetti() <= h.getHakemus().getPrioriteetti()) {
				if (h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY) {
					h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
					uudelleenSijoiteltavatHakukohteet.add(h);
				} else {
					if (h.getHakemus().getTila() == HakemuksenTila.VARALLA) {
						h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
					}
				}
			}
		}
		//
		// System.out.println("hyväksy: " + hakemus.getHenkilo().getHakijaOid()
		// + " uudelleensijoiteltavat: " +
		// uudelleenSijoiteltavatHakukohteet.size());

		return uudelleenSijoiteltavatHakukohteet;
	}

	// private int tilaaJonossa(ValintatapajonoWrapper valintatapajonoWrapper) {
	// Valintatapajono valintatapajono =
	// valintatapajonoWrapper.getValintatapajono();
	// int jaljellaolevatpaikat = valintatapajono.getAloituspaikat();
	// for (Hakemus h : valintatapajono.getHakemukset()) {
	// if (h.getTila() == HakemuksenTila.HYVAKSYTTY) {
	// jaljellaolevatpaikat--;
	// }
	// }
	// return jaljellaolevatpaikat;
	// }

	private boolean saannotSallii(HakemusWrapper hakemusWrapper) {
		Hakemus hakemus = hakemusWrapper.getHakemus();
		return hakemus.getTila() != HakemuksenTila.HYLATTY;
	}

	private boolean hakijaHaluaa(HakemusWrapper hakemusWrapper) {
		HenkiloWrapper henkilo = hakemusWrapper.getHenkilo();
		for (HakemusWrapper h : henkilo.getHakemukset()) {
			if (h.getHakemus().getTila() != null && h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY && h.getHakemus().getPrioriteetti() <= hakemusWrapper.getHakemus().getPrioriteetti() && hakemusWrapper != h) {
				return false;
			}
		}
		return true;
	}

	// private String getTasasijasaanto(ValintatapajonoWrapper
	// valintatapajonoWrapper) {
	// Valintatapajono j = valintatapajonoWrapper.getValintatapajono();
	// String tasasijasaantoNimi = null;
	// for (Saanto s : j.getSaannot()) {
	// if ("TASASIJA".equals(s.getTyyppi())) {
	// if (tasasijasaantoNimi == null) {
	// tasasijasaantoNimi = s.getNimi();
	// } else {
	// throw new SijoitteluFailedException("Valintatapajono [" + j.getOid() +
	// " has multiple TASASIJA sijoittelusaantos");
	// }
	// }
	// }
	// if (tasasijasaantoNimi == null || tasasijasaantoNimi.isEmpty()) {
	// throw new SijoitteluFailedException("Valintatapajono [" + j.getOid() +
	// " has zero TASASIJA sijoittelusaantos");
	// }
	// return tasasijasaantoNimi;
	// // return "YLITAYTTO";
	// }

	// private HakemusWrapper
	// haeParasRyhmaanKuuluvaValituksiHaluava(HakijaryhmaWrapper
	// hakijaryhmaWrapper, ValintatapajonoWrapper valintatapajonoWrapper) {
	// for (HakemusWrapper hakemusWrapper :
	// valintatapajonoWrapper.getHakemukset()) {
	// if
	// (hakijaryhmaWrapper.getHenkiloWrappers().contains(hakemusWrapper.getHenkilo())
	// && hakijaHaluaa(hakemusWrapper) && saannotSallii(hakemusWrapper)) {
	// return hakemusWrapper;
	// }
	// }
	// return null;
	// }

	private HakemusWrapper haeHuonoinValittuEiVajaaseenRyhmaanKuuluva(ValintatapajonoWrapper valintatapajonoWrapper) {
		HakemusWrapper huonoinHakemus = null;
		for (int i = valintatapajonoWrapper.getHakemukset().size() - 1; i >= 0; i--) {
			HakemusWrapper h = valintatapajonoWrapper.getHakemukset().get(i);
			if (h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY) {
				if (voidaanKorvata(h)) {
					huonoinHakemus = h;
					break;
				}
			}
		}

		return huonoinHakemus;
	}

	private int hakijaryhmassaVajaata(HakijaryhmaWrapper hakijaryhmaWrapper) {
		int needed = hakijaryhmaWrapper.getHakijaryhma().getPaikat();
		for (ValintatapajonoWrapper valintatapajonoWrapper : hakijaryhmaWrapper.getHakukohdeWrapper().getValintatapajonot()) {
			for (HakemusWrapper h : valintatapajonoWrapper.getHakemukset()) {
				if (h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY && hakijaryhmaWrapper.getHenkiloWrappers().contains(h.getHenkilo())) {
					needed--;
				}
			}
		}
		return needed;
	}

}
