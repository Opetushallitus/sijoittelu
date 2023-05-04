package fi.vm.sade.sijoittelu.batch.logic.impl;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.dto.ValintatietoValintatapaJonoRikastettuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.*;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;
import fi.vm.sade.valintalaskenta.domain.valinta.JarjestyskriteerituloksenTila;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DomainConverter {
    public static Hakukohde convertToHakukohde(HakukohdeDTO hakukohdeTyyppi) {
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid(hakukohdeTyyppi.getOid());
        hakukohde.setTarjoajaOid(hakukohdeTyyppi.getTarjoajaoid());
        hakukohde.setKaikkiJonotSijoiteltu(hakukohdeTyyppi.isKaikkiJonotSijoiteltu());
        addValintatapaJonos(hakukohdeTyyppi, hakukohde);
        addHakijaRyhmas(hakukohdeTyyppi, hakukohde);
        return hakukohde;
    }

    private static void addValintatapaJonos(HakukohdeDTO hakukohdeTyyppi, Hakukohde hakukohde) {
        hakukohdeTyyppi.getValinnanvaihe().forEach(vaihe ->
            convertJono(sijoiteltavaksiValmiitAktiivisetValintatapaJonot(vaihe.getValintatapajonot()), hakukohde));
    }

    private static List<ValintatietoValintatapajonoDTO> sijoiteltavaksiValmiitAktiivisetValintatapaJonot(List<ValintatietoValintatapajonoDTO> valintatapajonot) {
        return valintatapajonot.stream().filter(jono ->
                        jono.isSiirretaanSijoitteluun()
                                && (jono.getValmisSijoiteltavaksi() == null || jono.getValmisSijoiteltavaksi())
                                && (jono.getAktiivinen() == null || jono.getAktiivinen())
        ).collect(Collectors.toList());
    }

    private static void convertJono(List<ValintatietoValintatapajonoDTO> jonot, Hakukohde hakukohde) {
        jonot.forEach(valintatapajonoTyyppi -> {
            Valintatapajono valintatapajono = new Valintatapajono();
            valintatapajono.setOid(valintatapajonoTyyppi.getOid());
            valintatapajono.setNimi(valintatapajonoTyyppi.getNimi());
            valintatapajono.setPrioriteetti(valintatapajonoTyyppi.getPrioriteetti());
            valintatapajono.setAloituspaikat(valintatapajonoTyyppi.getAloituspaikat());
            try {
                valintatapajono.setTasasijasaanto(Tasasijasaanto.valueOf(valintatapajonoTyyppi.getTasasijasaanto().toString()));
            } catch (Exception e) {
                System.out.println("Valintatapajonon tasasijasääntöä ei tunnistettu: " + valintatapajonoTyyppi.getTasasijasaanto());
                valintatapajono.setTasasijasaanto(Tasasijasaanto.ARVONTA);
            }
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

            if (valintatapajonoTyyppi instanceof  ValintatietoValintatapaJonoRikastettuDTO) {
                valintatapajono.setMerkitseMyohAuto(((ValintatietoValintatapaJonoRikastettuDTO) valintatapajonoTyyppi).getMerkitseMyohAuto());
            }

            if (valintatapajonoTyyppi.getPoissaOlevaTaytto() != null && valintatapajonoTyyppi.getPoissaOlevaTaytto()) {
                valintatapajono.setPoissaOlevaTaytto(true);
            } else {
                valintatapajono.setPoissaOlevaTaytto(false);
            }
            valintatapajono.setTayttojono(valintatapajonoTyyppi.getTayttojono());
            valintatapajono.setVarasijat(valintatapajonoTyyppi.getVarasijat());
            valintatapajono.setVarasijaTayttoPaivat(valintatapajonoTyyppi.getVarasijaTayttoPaivat());
            valintatapajono.setVarasijojaKaytetaanAlkaen(valintatapajonoTyyppi.getVarasijojaKaytetaanAlkaen());
            valintatapajono.setVarasijojaTaytetaanAsti(valintatapajonoTyyppi.getVarasijojaTaytetaanAsti());
            hakukohde.getValintatapajonot().add(valintatapajono);
            valintatapajonoTyyppi.getHakija().forEach(hakija -> addHakemus(hakija, valintatapajono));
        });
    }

    private static void addHakijaRyhmas(HakukohdeDTO hakukohdeTyyppi, Hakukohde hakukohde) {
        for (HakijaryhmaDTO h : hakukohdeTyyppi.getHakijaryhma()) {
            Hakijaryhma hakijaryhma = new Hakijaryhma();
            hakijaryhma.setKiintio(h.getKiintio());
            hakijaryhma.setNimi(h.getNimi());
            hakijaryhma.setOid(h.getHakijaryhmaOid());
            hakijaryhma.setPrioriteetti(h.getPrioriteetti());
            hakijaryhma.setKaytaKaikki(h.isKaytaKaikki());
            hakijaryhma.setKaytetaanRyhmaanKuuluvia(h.isKaytetaanRyhmaanKuuluvia());
            hakijaryhma.setHakijaryhmatyyppikoodiUri(h.getHakijaryhmatyyppikoodiUri());
            hakijaryhma.setHakukohdeOid(h.getHakukohdeOid());
            hakijaryhma.setTarkkaKiintio(h.isTarkkaKiintio());
            hakijaryhma.setValintatapajonoOid(h.getValintatapajonoOid());
            // Tarkistetaan inversio vipu hakijaryhmasta
            final JarjestyskriteerituloksenTila filtteriEhto = h.isKaytetaanRyhmaanKuuluvia() ?
                    JarjestyskriteerituloksenTila.HYVAKSYTTAVISSA : JarjestyskriteerituloksenTila.HYLATTY;
            List<JonosijaDTO> hyvaksytyt = h.getJonosijat().stream()
                    .filter(j -> filtteriEhto.equals(j.getJarjestyskriteerit().first().getTila()))
                    .collect(Collectors.toList());
            for (JonosijaDTO jonosija : hyvaksytyt) {
                hakijaryhma.getHakemusOid().add(jonosija.getHakemusOid());
            }
            hakukohde.getHakijaryhmat().add(hakijaryhma);
        }
    }

    private static void addHakemus(HakijaDTO hakijaTyyppi, Valintatapajono valintatapajono) {
        Hakemus hakemus = new Hakemus();
        hakemus.setHakijaOid(hakijaTyyppi.getOid());
        hakemus.setHakemusOid(hakijaTyyppi.getHakemusOid());
        hakemus.setJonosija(hakijaTyyppi.getJonosija());
        hakemus.setTasasijaJonosija(hakijaTyyppi.getTasasijaJonosija());
        hakemus.setPrioriteetti(hakijaTyyppi.getPrioriteetti());
        hakemus.setEtunimi(hakijaTyyppi.getEtunimi());
        hakemus.setSukunimi(hakijaTyyppi.getSukunimi());
        if (hakijaTyyppi.getPisteet() != null) {
            hakemus.setPisteet(hakijaTyyppi.getPisteet());
        }
        applyPistetiedot(hakemus, hakijaTyyppi.getSyotettyArvo());
        if (hakijaTyyppi.getTila() == JarjestyskriteerituloksenTilaDTO.HYVAKSYTTY_HARKINNANVARAISESTI) {
            TilojenMuokkaus.asetaTilaksiVaralla(hakemus);
            hakemus.setHyvaksyttyHarkinnanvaraisesti(true);
        } else if (hakijaTyyppi.getTila() == JarjestyskriteerituloksenTilaDTO.HYVAKSYTTAVISSA) {
            TilojenMuokkaus.asetaTilaksiVaralla(hakemus);
        } else if (hakijaTyyppi.isHylattyValisijoittelussa()) {
            TilojenMuokkaus.asetaTilaksiVaralla(hakemus);
        } else {
            Map<String, String> tilanKuvaukset = hakijaTyyppi.getTilanKuvaus().parallelStream().collect(Collectors.toMap(AvainArvoDTO::getAvain, AvainArvoDTO::getArvo));
            TilojenMuokkaus.asetaTilaksiHylatty(hakemus, tilanKuvaukset);
        }
        valintatapajono.getHakemukset().add(hakemus);
    }

    private static void applyPistetiedot(Hakemus hakemus, List<SyotettyArvoDTO> arvot) {
        arvot.forEach(arvo -> {
            Pistetieto pistetieto = new Pistetieto();
            pistetieto.setArvo(arvo.getArvo());
            pistetieto.setLaskennallinenArvo(arvo.getLaskennallinenArvo());
            pistetieto.setOsallistuminen(arvo.getOsallistuminen());
            pistetieto.setTunniste(arvo.getTunniste());
            pistetieto.setTyypinKoodiUri(arvo.getTyypinKoodiUri());
            pistetieto.setTilastoidaan(arvo.isTilastoidaan());
            hakemus.getPistetiedot().add(pistetieto);
        });
    }
}
