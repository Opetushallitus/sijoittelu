package fi.vm.sade.sijoittelu.batch.logic.impl;




import fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi;
import fi.vm.sade.service.valintatiedot.schema.ValintatapajonoTyyppi;
import fi.vm.sade.service.valintatiedot.schema.ValinnanvaiheTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakijaTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakijaryhmaTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakemusTilaTyyppi;

import fi.vm.sade.sijoittelu.domain.Hakijaryhma;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;


/**
 *
 * @author Kari Kammonen
 *
 */
public class DomainConverter {

    public static Hakukohde convertToHakukohde(HakukohdeTyyppi hakukohdeTyyppi) {
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid(hakukohdeTyyppi.getOid());
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
                valintatapajono.setPrioriteetti(valintatapajonoTyyppi.getPrioriteetti());
                valintatapajono.setAloituspaikat(valintatapajonoTyyppi.getAloituspaikat());
                valintatapajono.setTasasijasaanto(Tasasijasaanto.valueOf(valintatapajonoTyyppi.getTasasijasaanto().toString()));

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
                hakijaryhma.getHakijaOid().add(s);
            }
            hakukohde.getHakijaryhmat().add(hakijaryhma);
        }
    }

    /**
     *
     * Luo hakemus valintatapajonoon. Pitaa sisallaan kohteen prioriteetin
     * hakijalle, sijan jonossa, pisteet, etc. Naita luodaan per valintatapajono
     * per henkilo
     *
     * @param hakijaTyyppi
     * @param valintatapajono
     * @return
     */
    private static void addHakemus(HakijaTyyppi hakijaTyyppi, Valintatapajono valintatapajono) {
        Hakemus hakemus = new Hakemus();
        hakemus.setHakijaOid(hakijaTyyppi.getOid());
        hakemus.setHakemusOid(hakijaTyyppi.getHakemusOid());
        hakemus.setJonosija(hakijaTyyppi.getJonosija());
        hakemus.setPrioriteetti(hakijaTyyppi.getPrioriteetti());

        if(hakijaTyyppi.isHarkinnanvarainen() ) {
            hakemus.setHarkinnanvarainen(true);
        }

        if (hakijaTyyppi.getTila() != null && hakijaTyyppi.getTila() == HakemusTilaTyyppi.HYVAKSYTTAVISSA) {
            // jos hyvaksyttavissa niin varalla, muuten hylatty
            hakemus.setTila(HakemuksenTila.VARALLA);
        } else {
            hakemus.setTila(HakemuksenTila.HYLATTY);
        }

        valintatapajono.getHakemukset().add(hakemus);
    }

}
