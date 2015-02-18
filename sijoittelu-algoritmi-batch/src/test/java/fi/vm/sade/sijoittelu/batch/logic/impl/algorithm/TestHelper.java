package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Sets;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import junit.framework.Assert;
import org.codehaus.jackson.map.DeserializationConfig;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static Hakukohde hakukohde(String endsWith, SijoitteluajoWrapper ajo) {
        return ajo.sijoitteluAjonHakukohteet().filter(h -> h.getOid().endsWith(endsWith)).findFirst().get();
    }
    public static Function<SijoitteluajoWrapper, Hakukohde> hakukohde(String endsWith) {
        return (ajo) -> hakukohde(endsWith,ajo);
    }
    public static Function<Hakukohde, Valintatapajono> valintatapajono(String endsWith) {
        return (hakukohde) -> hakukohde.getValintatapajonot().stream().filter(v -> v.getOid().endsWith(endsWith)).findFirst().get();
    }
    public static Function<Valintatapajono, Void> hyvaksyttyjaHakemuksiaAinoastaan(String ... endsWith) {
        final Set<String> endings = Sets.newHashSet(endsWith);
        return (valintatapajono) -> {
            List<Hakemus> ylimaaraisetHyvaksytytHakemukset =
            valintatapajono.getHakemukset().stream().filter(h -> HakemuksenTila.HYVAKSYTTY.equals(h.getTila()) && !endings.stream().anyMatch(e -> h.getHakemusOid().endsWith(e))).collect(Collectors.toList());

            List<Hakemus> puuttuneetHakemukset =
                    valintatapajono.getHakemukset().stream().filter(h -> endings.stream().anyMatch(e -> h.getHakemusOid().endsWith(e) && !HakemuksenTila.HYVAKSYTTY.equals(h.getTila()))).collect(Collectors.toList());
            if(!ylimaaraisetHyvaksytytHakemukset.isEmpty() && !puuttuneetHakemukset.isEmpty()) {
                Assert.fail("Valintatapajonossa oli ylimääräisiä hyväksyttyjä hakemuksia " + toString(ylimaaraisetHyvaksytytHakemukset) + " ja puuttuvia hyväksytyksi odotettuja hakemuksia " + toString(puuttuneetHakemukset));
            } else {
                if(!ylimaaraisetHyvaksytytHakemukset.isEmpty()) {
                    Assert.fail("Valintatapajonossa oli ylimääräisiä hyväksyttyjä hakemuksia " + toString(ylimaaraisetHyvaksytytHakemukset));
                } else if(!puuttuneetHakemukset.isEmpty()) {
                    Assert.fail("Valintatapajonossa oli puuttuvia hyväksytyksi odotettuja hakemuksia " + toString(puuttuneetHakemukset));
                }
            }

            return null;
        };
    }

    public static Function<Valintatapajono, Void> eiHyvaksyttyja() {
        return (valintatapajono) -> {
            List<Hakemus> hyvaksytytHakemukset =
                    valintatapajono.getHakemukset().stream().filter(h -> HakemuksenTila.HYVAKSYTTY.equals(h.getTila())).collect(Collectors.toList());

            if(!hyvaksytytHakemukset.isEmpty()) {
                Assert.fail("Valintatapajonossa oli hyväksyttyjä hakemuksia " + toString(hyvaksytytHakemukset));
            }

            return null;
        };
    }

    private final static String toString(List<Hakemus> h) {
        return Arrays.toString(h.stream().map(h0 -> h0.getHakemusOid()).toArray());
    }
    public final static void assertoi(SijoitteluajoWrapper ajo, Function<SijoitteluajoWrapper, Hakukohde> hakukohdeFunction, Function<Hakukohde, Valintatapajono> valintatapajonoFunction,
                                      Function<Valintatapajono, Void> hakemuksetFunction
    ) {
        hakukohdeFunction.andThen(valintatapajonoFunction).andThen(hakemuksetFunction).apply(ajo);
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
