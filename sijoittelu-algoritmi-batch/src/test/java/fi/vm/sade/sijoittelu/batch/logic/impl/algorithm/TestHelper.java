package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.service.valintatiedot.schema.HakuTyyppi;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import junit.framework.Assert;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public final class TestHelper {

    private TestHelper() {
    }

    public static HakuTyyppi xmlToObjects(String filename) {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            JAXBContext jc = JAXBContext.newInstance("fi.vm.sade.service.sijoittelu.types");
            Unmarshaller u = jc.createUnmarshaller();
            Object o = ((JAXBElement) u.unmarshal(is)).getValue();
            HakuTyyppi t = (HakuTyyppi) o;
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String objectsToXml(HakuTyyppi sijoitteluTyyppi) {
        try {
            JAXBContext jc = JAXBContext.newInstance("fi.vm.sade.service.sijoittelu.types");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter sw = new StringWriter();
            m.marshal(sijoitteluTyyppi, sw);
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Assertoi sijoittelultulokset
     * 
     * @param h
     * @param oids
     */
    public final static void assertoiAinoastaanValittu(Valintatapajono h, String... oids) {
        List<String> wanted = Arrays.asList(oids);
        List<String> actual = new ArrayList<String>();
        for (Hakemus hakemus : h.getHakemukset()) {
            if (hakemus.getTila() == HakemuksenTila.HYVAKSYTTY) {
                actual.add(hakemus.getHakemusOid());
            }
        }
        Assert.assertTrue("Actual result does not contain all wanted approved OIDs", actual.containsAll(wanted));
        Assert.assertTrue("Wanted result contains more approved OIDs than actual", wanted.containsAll(actual));
    }

    public final static void assertoiAinakinValittu(Valintatapajono h, String... oids) {
        List<String> wanted = Arrays.asList(oids);
        List<String> actual = new ArrayList<String>();
        for (Hakemus hakemus : h.getHakemukset()) {
            if (hakemus.getTila() == HakemuksenTila.HYVAKSYTTY) {
                actual.add(hakemus.getHakemusOid());
            }
        }
        Assert.assertTrue("Actual result does not contain all wanted approved OIDs", actual.containsAll(wanted));
    }

    public static void assertoiVainYksiJoukostaValittu(Valintatapajono valintatapajono, String... string) {
        List<Hakemus> hyvaksytty = new ArrayList<Hakemus>();

        for (String s : string) {
            for (Hakemus hakemus : valintatapajono.getHakemukset()) {
                if (hakemus.getHakemusOid().equals(s) && hakemus.getTila() == HakemuksenTila.HYVAKSYTTY) {
                    hyvaksytty.add(hakemus);
                }
            }
        }

        Assert.assertTrue("From list of OIDS: [...] was in hyvaksytty state[" + hyvaksytty.size() + "]",
                hyvaksytty.size() == 1);

    }

    public static void assertoiKukaanEiValittu(Valintatapajono valintatapajono) {
        for (Hakemus hakemus : valintatapajono.getHakemukset()) {
            if (hakemus.getTila() == HakemuksenTila.HYVAKSYTTY) {
                Assert.assertTrue("HAKEMUS " + hakemus.getHakemusOid() + " WAS HYVAKSYTTY", false);
            }
        }
    }

    public static void assertoi(Valintatapajono valintatapajono, String oid, HakemuksenTila tila) {
        Hakemus check = null;

        for (Hakemus hakemus : valintatapajono.getHakemukset()) {
            if(hakemus.getHakemusOid().equals(oid)) {
                check = hakemus;
            }
        }

        Assert.assertTrue(check.getTila().equals(tila));

    }
}
