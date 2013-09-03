package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import fi.vm.sade.sijoittelu.domain.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TestDataGenerator {

    public static final String HAKU_OID = "1.2.246.562.11.00000000001";
    public static final Long SIJOITTELU_ID_1 = 222L;
    public static final Long SIJOITTELU_ID_2 = 223L;

    public static final Long SIJOITTELU_AJO_ID_1 = 666L;
    public static final Long SIJOITTELU_AJO_ID_2 = 667L;

    public static final String HAKUKOHDE_OID_1 = "1.2.246.562.11.00000000002";
    public static final String HAKUKOHDE_OID_2 = "1.2.246.562.11.00000000003";
    public static final String HAKUKOHDE_OID_3 = "1.2.246.562.11.00000000004";

    public static final String VALINTATAPAJONO_OID_1 = "1.2.246.562.11.00000000005";
    public static final String VALINTATAPAJONO_OID_2 = "1.2.246.562.11.00000000006";

    public static final String HAKEMUS_OID_1 = "1.2.246.562.11.00000000007";
    public static final String HAKEMUS_OID_2 = "1.2.246.562.11.00000000008";
    public static final String HAKEMUS_OID_3 = "1.2.246.562.11.00000000009";
    public static final String HAKEMUS_OID_4 = "1.2.246.562.11.00000000010";


    public Sijoittelu generateTestData() {
        Sijoittelu sijoittelu = new Sijoittelu();
        sijoittelu.setHakuOid(HAKU_OID);
        sijoittelu.setSijoitteluId(SIJOITTELU_ID_1);

        SijoitteluAjo sijoitteluAjo1 = createSijoitteluajo1();
        SijoitteluAjo sijoitteluAjo2 = createSijoitteluajo2();
        sijoittelu.getSijoitteluajot().add(sijoitteluAjo1);
        sijoittelu.getSijoitteluajot().add(sijoitteluAjo2);


        List<Hakukohde> a1 = createHakukohdes(0);
        addHakukohdes(a1, sijoitteluAjo1)  ;
        List<Hakukohde> a2 = createHakukohdes(1);
        addHakukohdes(a2, sijoitteluAjo2)  ;


        return sijoittelu;
    }

    private void addHakukohdes(  List<Hakukohde> a1, SijoitteluAjo sijoitteluAjo ) {
        for(Hakukohde hk : a1) {
            HakukohdeItem item = new HakukohdeItem();
            item.setOid(hk.getOid());
            hk.setSijoitteluajoId(sijoitteluAjo.getSijoitteluajoId());
            sijoitteluAjo.getHakukohteet().add(item);
        }
    }

    private SijoitteluAjo createSijoitteluajo1() {

        Calendar startTime = Calendar.getInstance();
        startTime.set(2012, 9, 17, 10, 23);
        Calendar endTime = Calendar.getInstance();
        endTime.set(2012, 9, 20, 10, 24);

        SijoitteluAjo ajo1 = new SijoitteluAjo();
        ajo1.setStartMils(startTime.getTimeInMillis());
        ajo1.setEndMils(endTime.getTimeInMillis());
        ajo1.setSijoitteluajoId(SIJOITTELU_AJO_ID_1);


        return ajo1;
    }

    private SijoitteluAjo createSijoitteluajo2() {

        Calendar startTime = Calendar.getInstance();

        Calendar endTime = Calendar.getInstance();

        startTime.set(2012, 9, 21, 10, 23);
        endTime.set(2012, 9, 25, 10, 24);
        SijoitteluAjo ajo2 = new SijoitteluAjo();
        ajo2.setStartMils(startTime.getTimeInMillis());
        ajo2.setEndMils(endTime.getTimeInMillis());
        ajo2.setSijoitteluajoId(SIJOITTELU_AJO_ID_2);

        return ajo2;
    }

    public List<Hakukohde> createHakukohdes(int sijoitteluajoNumber) {
        List<Hakukohde> hakukohdes = new ArrayList<Hakukohde>();

        for (int i = 0; i < 3; ++i) {
            Hakukohde hakukohde = new Hakukohde();

            if (sijoitteluajoNumber == 1) {
                hakukohde.setTila(HakukohdeTila.SIJOITELTU);
            } else {
                hakukohde.setTila(HakukohdeTila.SIJOITTELUSSA);
            }

            switch (i) {
                case 0:
                    hakukohde.setOid(HAKUKOHDE_OID_1);
                    break;
                case 1:
                    hakukohde.setOid(HAKUKOHDE_OID_2);
                    break;
                case 2:
                    hakukohde.setOid(HAKUKOHDE_OID_3);
                    break;
                default:
                    break;
            }

            hakukohde.getValintatapajonot().addAll(createValintatapajonos());
            hakukohdes.add(hakukohde);
        }

        return hakukohdes;
    }

    private List<Valintatapajono> createValintatapajonos() {
        List<Valintatapajono> valintatapajonos = new ArrayList<Valintatapajono>();

        for (int i = 0; i < 2; ++i) {
            Valintatapajono valintatapajono = new Valintatapajono();
            valintatapajono.setPrioriteetti(i + 1);
            valintatapajono.setTila(ValintatapajonoTila.SIJOITELTU);
            valintatapajono.setAloituspaikat(3);
            switch (i) {
                case 0:
                    valintatapajono.setOid(VALINTATAPAJONO_OID_1);
                    break;

                case 1:
                    valintatapajono.setOid(VALINTATAPAJONO_OID_2);
                    break;

                default:
                    break;
            }
            valintatapajono.getHakemukset().addAll(createHakemus());
            valintatapajonos.add(valintatapajono);
        }

        return valintatapajonos;
    }

    private List<Hakemus> createHakemus() {
        List<Hakemus> hakemusList = new ArrayList<Hakemus>();
        for (int i = 0; i < 4; ++i) {
            Hakemus hakemus = new Hakemus();
            switch (i) {
                case 0:
                    hakemus.setHakijaOid(HAKEMUS_OID_1);
                    hakemus.setHakemusOid(HAKEMUS_OID_1);
                    hakemus.setJonosija(i + 1);
                    hakemus.setPrioriteetti(i + 1);
                    hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                    break;
                case 1:
                    hakemus.setHakijaOid(HAKEMUS_OID_2);
                    hakemus.setHakemusOid(HAKEMUS_OID_2);
                    hakemus.setJonosija(i + 1);
                    hakemus.setPrioriteetti(i + 1);
                    hakemus.setTila(HakemuksenTila.VARALLA);
                    break;
                case 2:
                    hakemus.setHakijaOid(HAKEMUS_OID_3);
                    hakemus.setHakemusOid(HAKEMUS_OID_3);
                    hakemus.setJonosija(i + 1);
                    hakemus.setPrioriteetti(i + 1);
                    hakemus.setTila(HakemuksenTila.VARALLA);
                    break;
                case 3:
                    hakemus.setHakijaOid(HAKEMUS_OID_4);
                    hakemus.setHakemusOid(HAKEMUS_OID_4);
                    hakemus.setJonosija(i + 1);
                    hakemus.setPrioriteetti(i + 1);
                    hakemus.setTila(HakemuksenTila.PERUUNTUNUT);
                    break;
                default:
                    break;
            }

            hakemusList.add(hakemus);
        }

        return hakemusList;
    }
}
