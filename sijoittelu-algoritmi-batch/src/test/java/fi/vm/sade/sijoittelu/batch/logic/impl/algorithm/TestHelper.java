package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import junit.framework.Assert;
import org.codehaus.jackson.map.DeserializationConfig;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
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

    public static HakuDTO xmlToObjects(String filename) {
        try {
            ObjectMapper xmlMapper = new ObjectMapper();
            xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            HakuDTO dto = xmlMapper.readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream(filename), HakuDTO.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String objectsToXml(HakuDTO sijoitteluTyyppi) {
        try {
//            JAXBContext jc = JAXBContext.newInstance("fi.vm.sade.service.sijoittelu.types");
//            Marshaller m = jc.createMarshaller();
//            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            StringWriter sw = new StringWriter();
//            m.marshal(sijoitteluTyyppi, sw);
            ObjectMapper xmlMapper = new XmlMapper();
            String xml = xmlMapper.writeValueAsString(sijoitteluTyyppi);
            return xml;
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
