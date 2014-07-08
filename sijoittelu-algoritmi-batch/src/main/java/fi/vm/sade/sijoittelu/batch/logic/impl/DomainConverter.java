package fi.vm.sade.sijoittelu.batch.logic.impl;


import fi.vm.sade.service.valintatiedot.schema.*;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.valintalaskenta.domain.dto.*;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 *
 * @author Kari Kammonen
 *
 */
public class DomainConverter {

    public static Hakukohde convertToHakukohde(HakukohdeTyyppi hakukohdeTyyppi) {
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid(hakukohdeTyyppi.getOid());
        hakukohde.setTarjoajaOid(hakukohdeTyyppi.getTarjoajaOid());
        addValintatapaJonos(hakukohdeTyyppi, hakukohde);
        addHakijaRyhmas(hakukohdeTyyppi, hakukohde);
        return hakukohde;
    }

    public static Hakukohde convertToHakukohdeRest(HakukohdeDTO hakukohdeTyyppi) {
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid(hakukohdeTyyppi.getOid());
        hakukohde.setTarjoajaOid(hakukohdeTyyppi.getTarjoajaoid());
        addValintatapaJonosRest(hakukohdeTyyppi, hakukohde);
        addHakijaRyhmasRest(hakukohdeTyyppi, hakukohde);
        return hakukohde;
    }

    private static void addValintatapaJonosRest(HakukohdeDTO hakukohdeTyyppi, Hakukohde hakukohde) {
        hakukohdeTyyppi.getValinnanvaihe().parallelStream().forEach(
                vaihe -> convertJono(vaihe.getValintatapajonot().stream().filter(
                        ValintatietoValintatapajonoDTO::isSiirretaanSijoitteluun).collect(Collectors.toList()), hakukohde));


    }

    private static void convertJono(List<ValintatietoValintatapajonoDTO> jonot, Hakukohde hakukohde) {

        jonot.parallelStream().forEach(valintatapajonoTyyppi -> {
            Valintatapajono valintatapajono = new Valintatapajono();
            valintatapajono.setOid(valintatapajonoTyyppi.getOid());
            valintatapajono.setNimi(valintatapajonoTyyppi.getNimi());
            valintatapajono.setPrioriteetti(valintatapajonoTyyppi.getPrioriteetti());
            valintatapajono.setAloituspaikat(valintatapajonoTyyppi.getAloituspaikat());
            valintatapajono.setTasasijasaanto(Tasasijasaanto.valueOf(valintatapajonoTyyppi.getTasasijasaanto().toString()));

            if (valintatapajonoTyyppi.getEiVarasijatayttoa() != null && valintatapajonoTyyppi.getEiVarasijatayttoa()) {
                valintatapajono.setEiVarasijatayttoa(true);
            } else {
                valintatapajono.setEiVarasijatayttoa(false);
            }

            if (valintatapajonoTyyppi.getKaikkiEhdonTayttavatHyvaksytaan() != null && valintatapajonoTyyppi.getKaikkiEhdonTayttavatHyvaksytaan()) {
                valintatapajono.setKaikkiEhdonTayttavatHyvaksytaan(true);
            } else {
                valintatapajono.setKaikkiEhdonTayttavatHyvaksytaan(false);
            }

            if (valintatapajonoTyyppi.getPoissaOlevaTaytto() != null && valintatapajonoTyyppi.getPoissaOlevaTaytto()) {
                valintatapajono.setPoissaOlevaTaytto(true);
            } else {
                valintatapajono.setPoissaOlevaTaytto(false);
            }

            hakukohde.getValintatapajonot().add(valintatapajono);

            valintatapajonoTyyppi.getHakija().parallelStream().forEach(hakija -> addHakemusRest(hakija, valintatapajono));

        });

    }

    private static void addValintatapaJonos(HakukohdeTyyppi hakukohdeTyyppi, Hakukohde hakukohde) {
        for(ValinnanvaiheTyyppi v: hakukohdeTyyppi.getValinnanvaihe()) {

            for (ValintatapajonoTyyppi valintatapajonoTyyppi : v.getValintatapajono()) {
                if(valintatapajonoTyyppi.isSiirretaanSijoitteluun()) {

                    Valintatapajono valintatapajono = new Valintatapajono();
                    valintatapajono.setOid(valintatapajonoTyyppi.getOid());
                    valintatapajono.setNimi(valintatapajonoTyyppi.getNimi());
                    valintatapajono.setPrioriteetti(valintatapajonoTyyppi.getPrioriteetti());
                    valintatapajono.setAloituspaikat(valintatapajonoTyyppi.getAloituspaikat());
                    valintatapajono.setTasasijasaanto(Tasasijasaanto.valueOf(valintatapajonoTyyppi.getTasasijasaanto().toString()));

                    if(valintatapajonoTyyppi.isEiVarasijatayttoa() != null && valintatapajonoTyyppi.isEiVarasijatayttoa()) {
                        valintatapajono.setEiVarasijatayttoa(true);
                    }     else {
                        valintatapajono.setEiVarasijatayttoa(false);
                    }

                    if(valintatapajonoTyyppi.isKaikkiEhdonTayttavatHyvaksytaan() != null && valintatapajonoTyyppi.isKaikkiEhdonTayttavatHyvaksytaan()) {
                        valintatapajono.setKaikkiEhdonTayttavatHyvaksytaan(true);
                    }     else {
                        valintatapajono.setKaikkiEhdonTayttavatHyvaksytaan(false);
                    }

                    if(valintatapajonoTyyppi.isPoissaOlevaTaytto() != null && valintatapajonoTyyppi.isPoissaOlevaTaytto()) {
                        valintatapajono.setPoissaOlevaTaytto(true);
                    }     else {
                        valintatapajono.setPoissaOlevaTaytto(false);
                    }

                    hakukohde.getValintatapajonot().add(valintatapajono);

                    for (HakijaTyyppi hakijaTyyppi : valintatapajonoTyyppi.getHakija()) {
                        addHakemus(hakijaTyyppi, valintatapajono);
                    }

                }
            }
        }
    }

    private static void addHakijaRyhmasRest(HakukohdeDTO hakukohdeTyyppi, Hakukohde hakukohde) {
        for (HakijaryhmaDTO h : hakukohdeTyyppi.getHakijaryhma()) {
            Hakijaryhma hakijaryhma = new Hakijaryhma();
            hakijaryhma.setPaikat(h.getPaikat());
            hakijaryhma.setNimi(h.getNimi());
            hakijaryhma.setOid(h.getOid());
            hakijaryhma.setPrioriteetti(h.getPrioriteetti());
            for (String s : h.getHakijaOids()) {
                hakijaryhma.getHakemusOid().add(s);
            }
            hakukohde.getHakijaryhmat().add(hakijaryhma);
        }
    }

    private static void addHakijaRyhmas(HakukohdeTyyppi hakukohdeTyyppi, Hakukohde hakukohde) {
        for (HakijaryhmaTyyppi h : hakukohdeTyyppi.getHakijaryhma()) {
            Hakijaryhma hakijaryhma = new Hakijaryhma();
            hakijaryhma.setPaikat(h.getPaikat());
            hakijaryhma.setNimi(h.getNimi());
            hakijaryhma.setOid(h.getOid());
            hakijaryhma.setPrioriteetti(h.getPrioriteetti());
            for (String s : h.getHakijaOid()) {
                hakijaryhma.getHakemusOid().add(s);
            }
            hakukohde.getHakijaryhmat().add(hakijaryhma);
        }
    }

    private static void addHakemusRest(HakijaDTO hakijaTyyppi, Valintatapajono valintatapajono) {
        Hakemus hakemus = new Hakemus();
        hakemus.setHakijaOid(hakijaTyyppi.getOid());
        hakemus.setHakemusOid(hakijaTyyppi.getHakemusOid());
        hakemus.setJonosija(hakijaTyyppi.getJonosija());
        hakemus.setTasasijaJonosija(hakijaTyyppi.getTasasijaJonosija());
        hakemus.setPrioriteetti(hakijaTyyppi.getPrioriteetti());
        hakemus.setEtunimi(hakijaTyyppi.getEtunimi());
        hakemus.setSukunimi(hakijaTyyppi.getSukunimi());

        if(hakijaTyyppi.getPisteet() != null) {
            hakemus.setPisteet(hakijaTyyppi.getPisteet());
        }

        applyPistetiedotRest(hakemus, hakijaTyyppi.getSyotettyArvo()) ;

        if(hakijaTyyppi.getTila() == JarjestyskriteerituloksenTilaDTO.HYVAKSYTTY_HARKINNANVARAISESTI)     {
            hakemus.setTila(HakemuksenTila.VARALLA);
            hakemus.setHyvaksyttyHarkinnanvaraisesti(true);
        } else if (hakijaTyyppi.getTila() == JarjestyskriteerituloksenTilaDTO.HYVAKSYTTAVISSA) {
            hakemus.setTila(HakemuksenTila.VARALLA);
        } else {
            Map<String,String> tilanKuvaukset = hakijaTyyppi.getTilanKuvaus().parallelStream().collect(Collectors.toMap(AvainArvoDTO::getAvain, AvainArvoDTO::getArvo));
            hakemus.setTilanKuvaukset(tilanKuvaukset);
            hakemus.setTila(HakemuksenTila.HYLATTY);
        }

        valintatapajono.getHakemukset().add(hakemus);
    }

    private static void addHakemus(HakijaTyyppi hakijaTyyppi, Valintatapajono valintatapajono) {
        Hakemus hakemus = new Hakemus();
        hakemus.setHakijaOid(hakijaTyyppi.getOid());
        hakemus.setHakemusOid(hakijaTyyppi.getHakemusOid());
        hakemus.setJonosija(hakijaTyyppi.getJonosija());
        hakemus.setTasasijaJonosija(hakijaTyyppi.getTasasijaJonosija());
        hakemus.setPrioriteetti(hakijaTyyppi.getPrioriteetti());
        hakemus.setEtunimi(hakijaTyyppi.getEtunimi());
        hakemus.setSukunimi(hakijaTyyppi.getSukunimi());

        if(hakijaTyyppi.getPisteet() != null && !hakijaTyyppi.getPisteet().isEmpty()) {
            hakemus.setPisteet(new BigDecimal(hakijaTyyppi.getPisteet()));
        }

        applyPistetiedot(hakemus, hakijaTyyppi.getSyotettyArvo()) ;

        if(hakijaTyyppi.getTila() == HakemusTilaTyyppi.HYVAKSYTTY_HARKINNANVARAISESTI)     {
            hakemus.setTila(HakemuksenTila.VARALLA);
            hakemus.setHyvaksyttyHarkinnanvaraisesti(true);
        } else if (hakijaTyyppi.getTila() == HakemusTilaTyyppi.HYVAKSYTTAVISSA) {
            hakemus.setTila(HakemuksenTila.VARALLA);
        } else {
            Map<String,String> tilanKuvaukset = new HashMap<String, String>();
            for (AvainArvoTyyppi tyyppi : hakijaTyyppi.getTilanKuvaus()) {
                tilanKuvaukset.put(tyyppi.getAvain(),tyyppi.getArvo());
            }
            hakemus.setTilanKuvaukset(tilanKuvaukset);
            hakemus.setTila(HakemuksenTila.HYLATTY);
        }

        valintatapajono.getHakemukset().add(hakemus);
    }

    private static void applyPistetiedotRest(Hakemus hakemus, List<SyotettyArvoDTO> arvot) {
        arvot.parallelStream().forEach(arvo -> {
            Pistetieto pistetieto = new Pistetieto();
            pistetieto.setArvo(arvo.getArvo());
            pistetieto.setLaskennallinenArvo(arvo.getLaskennallinenArvo());
            pistetieto.setOsallistuminen(arvo.getOsallistuminen());
            pistetieto.setTunniste(arvo.getTunniste());
            hakemus.getPistetiedot().add(pistetieto);
        });
    }

    private static void applyPistetiedot(Hakemus hakemus, List<SyotettyArvoTyyppi> arvot) {
        for(SyotettyArvoTyyppi arvo : arvot) {
            Pistetieto pistetieto = new Pistetieto();
            pistetieto.setArvo(arvo.getArvo());
            pistetieto.setLaskennallinenArvo(arvo.getLaskennallinenArvo());
            pistetieto.setOsallistuminen(arvo.getOsallistuminen());
            pistetieto.setTunniste(arvo.getTunniste());
            hakemus.getPistetiedot().add(pistetieto);
        }
    }

}
