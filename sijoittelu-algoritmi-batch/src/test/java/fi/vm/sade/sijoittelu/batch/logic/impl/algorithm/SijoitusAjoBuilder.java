package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

class SijoitusAjoBuilder {
    private List<Hakijaryhma> hakijaryhmat = new ArrayList<>();
    private List<JonoBuilder> jonot = new ArrayList<>();
    private boolean kaikkiJulkaistu = false;

    public static void assertSijoittelu(SijoitusAjoBuilder before, SijoitusAjoBuilder after) {
        SijoitteluajoWrapper sijoitteluajoWrapper = before.build();
        SijoitteluAlgorithm.sijoittele(sijoitteluajoWrapper);
        HakukohdeWrapper hakukohdeWrapper = sijoitteluajoWrapper.getHakukohteet().get(0);
        assertEquals(after.forTest(), SijoitusAjoBuilder.forTest(hakukohdeWrapper.getHakukohde()));
    }

    public static void luoAlkuTilanneJaAssertSijoittelu(SijoitusAjoBuilder after) {
        assertSijoittelu(after.buildBefore(), after);
    }

    public SijoitusAjoBuilder hakijaryhma(int kiintio, String... args) {
        createHakijaryhmaForValintatapajono(kiintio, null, args);
        return this;
    }

    private Hakijaryhma createHakijaryhmaForValintatapajono(int kiintio, String valintatapajonoOid, String[] hakemusOids) {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhmat.add(hakijaryhma);
        hakijaryhma.setKiintio(kiintio);
        hakijaryhma.getHakemusOid().addAll(Arrays.asList(hakemusOids));
        hakijaryhma.setPrioriteetti(hakijaryhmat.size() + 1);
        hakijaryhma.setValintatapajonoOid(valintatapajonoOid);
        return hakijaryhma;
    }

    public JonoBuilder jono(int aloituspaikat) {
        return jono(aloituspaikat, Tasasijasaanto.ARVONTA);
    }

    public JonoBuilder jono(int aloituspaikat, Tasasijasaanto tasasijasaanto) {
        JonoBuilder jonoBuilder = new JonoBuilder(aloituspaikat, tasasijasaanto, jonot.size() + 1);
        jonot.add(jonoBuilder);
        return jonoBuilder;
    }

    private SijoitusAjoBuilder buildBefore() {
        SijoitusAjoBuilder before = new SijoitusAjoBuilder();
        before.hakijaryhmat.addAll(hakijaryhmat);
        for (JonoBuilder jono : jonot) {
            JonoBuilder beforeJonoBuilder = new JonoBuilder(jono.aloituspaikat, jono.tasasijasaanto, jono.valintatapajonoPrioriteetti);
            for (Hakemus hakemus : jono.hakemukset) {
                HakemuksenTila tila = Arrays.asList(HakemuksenTila.PERUNUT).contains(hakemus.getTila()) ? hakemus.getTila() : HakemuksenTila.VARALLA;
                boolean julkaistu = jono.julkaistutHakemukset.contains(hakemus);
                beforeJonoBuilder.copyHakemusWithTila(hakemus, tila, julkaistu);
            }
            before.jonot.add(beforeJonoBuilder);
        }
        return before;
    }

    private Valintatapajono buildValintatapaJono(JonoBuilder j) {
        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setAloituspaikat(j.aloituspaikat);
        valintatapajono.setTasasijasaanto(j.tasasijasaanto);
        valintatapajono.setHakemukset(j.hakemukset);
        valintatapajono.setPrioriteetti(j.valintatapajonoPrioriteetti);
        valintatapajono.setOid(j.valintatapajonoOid);
        return valintatapajono;
    }

    private SijoitteluajoWrapper build() {
        SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
        sijoitteluAjo.setHakuOid("haku-1");
//        verify();
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("hakukohde-1");
        hakukohde.getHakijaryhmat().addAll(hakijaryhmat);

        List<Valintatulos> valintatulokset = new ArrayList<>();
        for (JonoBuilder jonoBuilder : jonot) {
            hakukohde.getValintatapajonot().add(buildValintatapaJono(jonoBuilder));
            List<Hakemus> julkaistutHakemukset = jonoBuilder.julkaistutHakemukset;
            if (kaikkiJulkaistu) {
                julkaistutHakemukset = jonoBuilder.hakemukset;
            }
            for (Hakemus h : julkaistutHakemukset) {
                valintatulokset.add(new Valintatulos(jonoBuilder.valintatapajonoOid, h.getHakemusOid(), hakukohde.getOid(), h.getHakijaOid(), sijoitteluAjo.getHakuOid(), h.getPrioriteetti()));
            }
        }

        return SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(sijoitteluAjo, Arrays.asList(hakukohde), valintatulokset);
    }

    private SijoitusAjoBuilder verify() {
        JonoBuilder firstJono = jonot.get(0);
        for (JonoBuilder jono : jonot) {
            assertEquals("Hakija-jonoissa virhe", jono.getHakijaNames(), firstJono.getHakijaNames());
        }
        return this;
    }

    private HashMap forTest() {
        return new HashMap() {{
            List<List<String>> jonoList = jonot.stream().map(j -> hakemuksetStringList(j.hakemukset)).collect(Collectors.toList());
            put("jonot", jonoList);
        }};
    }

    private static List<String> hakemuksetStringList(List<Hakemus> hakemukset) {
        return hakemukset.stream().map(h -> "" + h.getJonosija() + ":" + h.getTasasijaJonosija() + ":" + h.getHakijaOid() + ":" + h.getTila()).collect(Collectors.toList());
    }

    private static HashMap forTest(Hakukohde hakukohde) {
        return new HashMap() {{
            List<List<String>> jonoList = hakukohde.getValintatapajonot().stream().map(j -> hakemuksetStringList(j.getHakemukset())).collect(Collectors.toList());
            put("jonot", jonoList);
        }};
    }

    public SijoitusAjoBuilder julkaiseKaikki() {
        kaikkiJulkaistu = true;
        return this;
    }

    public class JonoBuilder {
        private final int aloituspaikat;
        private final List<Hakemus> hakemukset = new ArrayList<>();
        private final Tasasijasaanto tasasijasaanto;
        private final String valintatapajonoOid;
        private final int valintatapajonoPrioriteetti;
        private boolean samaJonoSija = false;
        private int tasasijaJonoSija;
        private int alkuperainenJonosija;
        private List<Hakemus> julkaistutHakemukset = new ArrayList<>();

        public JonoBuilder(int aloituspaikat, Tasasijasaanto tasasijasaanto, int valintatapajonoPrioriteetti) {
            this.aloituspaikat = aloituspaikat;
            this.tasasijasaanto = tasasijasaanto;
            this.valintatapajonoPrioriteetti = valintatapajonoPrioriteetti;
            this.valintatapajonoOid = "jono-" + valintatapajonoPrioriteetti;
        }

        public JonoBuilder hyvaksytty(String... hakijat) {
            add(HakemuksenTila.HYVAKSYTTY, false, hakijat);
            return this;
        }

        public JonoBuilder samaJonosija(Consumer<JonoBuilder> callable) {
            samaJonoSija = true;
            alkuperainenJonosija = hakemukset.size();
            tasasijaJonoSija = 1;
            callable.accept(this);
            samaJonoSija = false;
            return this;
        }

        private JonoBuilder add(HakemuksenTila tila, boolean julkaistu, String... hakijat) {
            for (String hakija : hakijat) {
                if (!samaJonoSija) {
                    tasasijaJonoSija = 1;
                }
                int jonosija = 1 + (samaJonoSija ? alkuperainenJonosija : hakemukset.size());
                Hakemus h = createHakemus(hakija, tila, jonosija, tasasijaJonoSija);
                hakemukset.add(h);
                if (julkaistu) {
                    julkaistutHakemukset.add(h);
                }
                tasasijaJonoSija++;
            }
            return this;
        }

        public JonoBuilder copyHakemusWithTila(Hakemus hakemus, HakemuksenTila tila, boolean julkaistu) {
            Hakemus h = createHakemus(hakemus.getHakemusOid(), tila, hakemus.getJonosija(), hakemus.getTasasijaJonosija());
            hakemukset.add(h);
            if (julkaistu) {
                julkaistutHakemukset.add(h);
            }
            return this;
        }

        private Hakemus createHakemus(String hakija, HakemuksenTila tila, int jonosija, int tasasijaJonoSija) {
            Hakemus h = new Hakemus();
            h.setHakemusOid(hakija);
            h.setHakijaOid(hakija);
            h.setTila(tila);
            h.setJonosija(jonosija);
            h.setTasasijaJonosija(tasasijaJonoSija);
            h.setPrioriteetti(1);
            return h;
        }

        public JonoBuilder varalla(String... hakijat) {
            return add(HakemuksenTila.VARALLA, false, hakijat);
        }

        public JonoBuilder peruuntunut(String... hakijat) {
            return add(HakemuksenTila.PERUUNTUNUT, false, hakijat);
        }

        public JonoBuilder perunut(String... hakijat) {
            return add(HakemuksenTila.PERUNUT, false, hakijat);
        }

        public JonoBuilder varalla_julkaistu(String... hakijat) {
            return add(HakemuksenTila.VARALLA, false, hakijat);
        }

        public JonoBuilder peruuntunut_julkaistu(String... hakijat) {
            return add(HakemuksenTila.PERUUNTUNUT, false, hakijat);
        }

        public JonoBuilder perunut_julkaistu(String... hakijat) {
            return add(HakemuksenTila.PERUNUT, false, hakijat);
        }

        public List<String> getHakijaNames() {
            List<String> names = hakemukset.stream().map(h -> h.getHakijaOid()).collect(Collectors.toList());
            Collections.sort(names);
            return names;
        }

        public JonoBuilder hakijaryhma(int kiintio, String... args) {
            createHakijaryhmaForValintatapajono(kiintio, valintatapajonoOid, args);
            return this;
        }
    }
}
