package fi.vm.sade.sijoittelu.batch.logic.impl;


import fi.vm.sade.service.valintatiedot.schema.*;
import fi.vm.sade.sijoittelu.domain.*;

import java.math.BigDecimal;
import java.util.List;


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

    private static void addValintatapaJonos(HakukohdeTyyppi hakukohdeTyyppi, Hakukohde hakukohde) {
        for(ValinnanvaiheTyyppi v: hakukohdeTyyppi.getValinnanvaihe()) {

            for (ValintatapajonoTyyppi valintatapajonoTyyppi : v.getValintatapajono()) {
                //  if(valintatapajonoTyyppi.isSiirretaanSijoitteluun()) {
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

                hakukohde.getValintatapajonot().add(valintatapajono);

                for (HakijaTyyppi hakijaTyyppi : valintatapajonoTyyppi.getHakija()) {
                    addHakemus(hakijaTyyppi, valintatapajono);
                }
                //     }
            }
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
        }  else if (hakijaTyyppi.getTila() == HakemusTilaTyyppi.HYVAKSYTTAVISSA) {
            hakemus.setTila(HakemuksenTila.VARALLA);
        } else {
            hakemus.setTila(HakemuksenTila.HYLATTY);
        }

        valintatapajono.getHakemukset().add(hakemus);
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
