package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Sets;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TestHelper {
    public static HakuDTO readHakuDTOFromJson(String filename) {
        try {
            return getDTO(filename, HakuDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static List<Hakukohde> readHakukohteetListFromJson(String filename) {
        try {
            return getDTO(filename, new TypeReference<List<Hakukohde>>(){});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Valintatulos> readValintatulosListFromJson(String filename) {
        try {
            return getDTO(filename, new TypeReference<List<Valintatulos>>(){});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static <T> T getDTO(String filename, Class<T> valueType) throws java.io.IOException {
        ObjectMapper xmlMapper = new ObjectMapper();
        xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return xmlMapper.readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream(filename), valueType);
    }

    static <T> T getDTO(String filename, TypeReference valueTypeRef) throws java.io.IOException {
        ObjectMapper xmlMapper = new ObjectMapper();
        xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return xmlMapper.readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream(filename), valueTypeRef);
    }

    private static Hakukohde hakukohde(String endsWith, SijoitteluajoWrapper ajo) {
        return ajo.sijoitteluAjonHakukohteet().filter(h -> h.getOid().endsWith(endsWith)).findFirst().get();
    }

    public static Function<SijoitteluajoWrapper, Hakukohde> hakukohde(String endsWith) {
        return (ajo) -> hakukohde(endsWith, ajo);
    }

    public static Function<Hakukohde, Valintatapajono> valintatapajono(String endsWith) {
        return (hakukohde) -> hakukohde.getValintatapajonot().stream().filter(v -> v.getOid().endsWith(endsWith)).findFirst().get();
    }

    public static Function<Valintatapajono, Void> hyvaksyttyjaHakemuksiaAinoastaan(String... endsWith) {
        return tilaisiaHakemuksiaAinoastaan(HakemuksenTila.HYVAKSYTTY, endsWith);
    }

    public static Function<Valintatapajono, Void> varallaAinoastaan(String... endsWith) {
        return tilaisiaHakemuksiaAinoastaan(HakemuksenTila.VARALLA, endsWith);
    }

    public static Function<Valintatapajono, Void> peruuntuneitaHakemuksiaAinoastaan(String... endsWith) {
        return tilaisiaHakemuksiaAinoastaan(HakemuksenTila.PERUUNTUNUT, endsWith);
    }

    private static Function<Valintatapajono, Void> tilaisiaHakemuksiaAinoastaan(HakemuksenTila hakemuksenTila, String... endsWith) {
        final Set<String> endings = Sets.newHashSet(endsWith);
        return (valintatapajono) -> {
            List<Hakemus> ylimaaraisetHyvaksytytHakemukset = valintatapajono.getHakemukset().stream()
                    .filter(h -> hakemuksenTila.equals(h.getTila()) && !endings.stream().anyMatch(e -> h.getHakemusOid().endsWith(e)))
                    .collect(Collectors.toList());
            List<Hakemus> puuttuneetHakemukset = valintatapajono.getHakemukset().stream()
                    .filter(h -> endings.stream().anyMatch(e -> h.getHakemusOid().endsWith(e) && !hakemuksenTila.equals(h.getTila())))
                    .collect(Collectors.toList());
            if (!ylimaaraisetHyvaksytytHakemukset.isEmpty() && !puuttuneetHakemukset.isEmpty()) {
                Assert.fail("Valintatapajonossa oli ylimääräisiä " + hakemuksenTila + " hakemuksia " + toString(ylimaaraisetHyvaksytytHakemukset) + " ja puuttuvia " + hakemuksenTila + " odotettuja hakemuksia " + toString(puuttuneetHakemukset));
            } else {
                if (!ylimaaraisetHyvaksytytHakemukset.isEmpty()) {
                    Assert.fail("Valintatapajonossa oli ylimääräisiä " + hakemuksenTila + " hakemuksia " + toString(ylimaaraisetHyvaksytytHakemukset));
                } else if (!puuttuneetHakemukset.isEmpty()) {
                    Assert.fail("Valintatapajonossa oli puuttuvia " + hakemuksenTila + " odotettuja hakemuksia " + toString(puuttuneetHakemukset));
                }
            }
            return null;
        };
    }

    public static Function<Valintatapajono, Void> eiHyvaksyttyja() {
        return (valintatapajono) -> {
            List<Hakemus> hyvaksytytHakemukset = valintatapajono.getHakemukset().stream().filter(h -> HakemuksenTila.HYVAKSYTTY.equals(h.getTila())).collect(Collectors.toList());
            if (!hyvaksytytHakemukset.isEmpty()) {
                Assert.fail("Valintatapajonossa oli hyväksyttyjä hakemuksia " + toString(hyvaksytytHakemukset));
            }
            return null;
        };
    }

    private static String toString(List<Hakemus> h) {
        return Arrays.toString(h.stream().map(h0 -> h0.getHakemusOid()).toArray());
    }

    public static void assertoi(SijoitteluajoWrapper ajo, Function<SijoitteluajoWrapper, Hakukohde> hakukohdeFunction, Function<Hakukohde, Valintatapajono> valintatapajonoFunction, Function<Valintatapajono, Void> hakemuksetFunction) {
        hakukohdeFunction.andThen(valintatapajonoFunction).andThen(hakemuksetFunction).apply(ajo);
    }

    public static String objectsToXml(HakuDTO sijoitteluTyyppi) {
        try {
            ObjectMapper xmlMapper = new XmlMapper();
            return xmlMapper.writeValueAsString(sijoitteluTyyppi);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void assertoiAinoastaanValittu(Valintatapajono h, String... oids) {
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

    public static void assertoiAinoastaanValittuMyosVarasijalta(Valintatapajono h, String... oids) {
        List<String> wanted = Arrays.asList(oids);
        List<String> actual = new ArrayList<String>();
        for (Hakemus hakemus : h.getHakemukset()) {
            if (hakemus.getTila() == HakemuksenTila.HYVAKSYTTY || hakemus.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                actual.add(hakemus.getHakemusOid());
            }
        }
        Assert.assertTrue("Actual result does not contain all wanted approved OIDs", actual.containsAll(wanted));
        Assert.assertTrue("Wanted result contains more approved OIDs than actual", wanted.containsAll(actual));
    }

    public static void assertoiAinakinValittu(Valintatapajono h, String... oids) {
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
        Assert.assertTrue("From list of OIDS: [...] was in hyvaksytty state[" + hyvaksytty.size() + "]", hyvaksytty.size() == 1);
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
            if (hakemus.getHakemusOid().equals(oid)) {
                check = hakemus;
            }
        }
        Assert.assertEquals(tila, check.getTila());
    }
}
