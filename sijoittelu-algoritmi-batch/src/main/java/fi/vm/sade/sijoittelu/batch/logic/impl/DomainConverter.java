package fi.vm.sade.sijoittelu.batch.logic.impl;

import fi.vm.sade.service.valintatiedot.schema.*;
import fi.vm.sade.sijoittelu.domain.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kari Kammonen
 *
 */
public class DomainConverter {

    /**
     * Luo sijoittelun domaini ja palauta algorimi/domain instanssi
     *
     * @param haku
     * @param hakukohdeTyyppit
     * @return
     */
    public static SijoitteluAjo convertToSijoitteluAjo(List<HakukohdeTyyppi> hakukohdeTyypit) {
        List<HakukohdeItem> hakukohteet = createDomain(hakukohdeTyypit);
        SijoitteluAjo ajo = new SijoitteluAjo();
        ajo.getHakukohteet().addAll(hakukohteet);
        return ajo;
    }

    /**
     *
     * @param hakukohdeTyyppit
     * @param hakukohteet
     */
    private static List<HakukohdeItem> createDomain(List<HakukohdeTyyppi> hakukohdeTyyppit) {
        // hakukohteet
        List<HakukohdeItem> hakukohdeItems = new ArrayList<HakukohdeItem>();

        for (HakukohdeTyyppi hakukohdeTyyppi : hakukohdeTyyppit) {
            Hakukohde hakukohde = new Hakukohde();
            hakukohde.setOid(hakukohdeTyyppi.getOid());

            HakukohdeItem hakukohdeItem = new HakukohdeItem();
            hakukohdeItem.setOid(hakukohdeTyyppi.getOid());
            hakukohdeItem.setHakukohde(hakukohde);

            hakukohdeItems.add(hakukohdeItem);

            addValintatapaJonos(hakukohdeTyyppi, hakukohde);
            addHakijaRyhmas(hakukohdeTyyppi, hakukohde);

        }
        return hakukohdeItems;
    }

    private static void addValintatapaJonos(HakukohdeTyyppi hakukohdeTyyppi, Hakukohde hakukohde) {
        for(ValinnanvaiheTyyppi v: hakukohdeTyyppi.getValinnanvaihe()) {


            for (ValintatapajonoTyyppi valintatapajonoTyyppi : v.getValintatapajono()) {
                if(valintatapajonoTyyppi.isSiirretaanSijoitteluun()) {
                    Valintatapajono valintatapajono = new Valintatapajono();
                    valintatapajono.setOid(valintatapajonoTyyppi.getOid());
                    valintatapajono.setPrioriteetti(valintatapajonoTyyppi.getPrioriteetti());
                    valintatapajono.setAloituspaikat(valintatapajonoTyyppi.getAloituspaikat());
			/*if (valintatapajonoTyyppi.getTila() != null) {
				valintatapajono.setTila(ValintatapajonoTila.valueOf(valintatapajonoTyyppi.getTila().name()));
			}*/

			/*
            for (SaantoTyyppi s : valintatapajonoTyyppi.getSaanto()) {
				Saanto saanto = new Saanto();
				saanto.setNimi(s.getNimi());
				saanto.setTyyppi(s.getTyyppi());
				saanto.getParameters().addAll(s.getParametri());
				valintatapajono.getSaannot().add(saanto);
			}
*/

                    valintatapajono.setTasasijasaanto(Tasasijasaanto.valueOf(valintatapajonoTyyppi.getTasasijasaanto().toString()));

                    hakukohde.getValintatapajonot().add(valintatapajono);

                    for (HakijaTyyppi hakijaTyyppi : valintatapajonoTyyppi.getHakija()) {
                        addHakemus(hakijaTyyppi, valintatapajono);
                    }
                }
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

        if (hakijaTyyppi.getTila() != null && hakijaTyyppi.getTila() == HakemusTilaTyyppi.HYVAKSYTTAVISSA) {
            // jos hyvaksyttavissa niin varalla, muuten hylatty
            hakemus.setTila(HakemuksenTila.VARALLA);
        } else {
            hakemus.setTila(HakemuksenTila.HYLATTY);
        }

        valintatapajono.getHakemukset().add(hakemus);
    }

}
