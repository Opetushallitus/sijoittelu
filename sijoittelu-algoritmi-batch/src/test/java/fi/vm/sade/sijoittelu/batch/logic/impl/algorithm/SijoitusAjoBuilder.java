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

    public static void assertSijoittelu(SijoitusAjoBuilder before, SijoitusAjoBuilder after) {
        SijoitteluajoWrapper sijoitteluajoWrapper = before.buildSijoitteluajoWrapper();
        SijoitteluAlgorithm.sijoittele(sijoitteluajoWrapper);
        HakukohdeWrapper hakukohdeWrapper = sijoitteluajoWrapper.getHakukohteet().get(0);
        assertEquals(after.forTest(), SijoitusAjoBuilder.forTest(hakukohdeWrapper.getHakukohde()));
    }

    public static void assertSijoittelu(SijoitusAjoBuilder after) {
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
        JonoBuilder jonoBuilder = new JonoBuilder(aloituspaikat, tasasijasaanto, Integer.toString(jonot.size()));
        jonot.add(jonoBuilder);
        return jonoBuilder;
    }

    public SijoitusAjoBuilder buildBefore() {
        SijoitusAjoBuilder before = new SijoitusAjoBuilder();
        before.hakijaryhmat.addAll(hakijaryhmat);
        for (JonoBuilder jono : jonot) {
            JonoBuilder beforeJonoBuilder = new JonoBuilder(jono.aloituspaikat, jono.tasasijasaanto, jono.valintatapajonoOid);
            for(Hakemus hakemus : jono.hakemukset) {
                HakemuksenTila tila = Arrays.asList(HakemuksenTila.PERUNUT).contains(hakemus.getTila()) ? hakemus.getTila() : HakemuksenTila.VARALLA;
                beforeJonoBuilder.copyHakemusWithTila(hakemus, tila);
            }
            before.jonot.add(beforeJonoBuilder);
        }
        return before;
    }

    private Hakukohde buildHakukohde() {
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("1");
        hakukohde.getValintatapajonot().addAll(jonot.stream().map(j -> buildValintatapaJono(j, jonot.indexOf(j))).collect(Collectors.toList()));
        hakukohde.getHakijaryhmat().addAll(hakijaryhmat);
        return hakukohde;
    }

    private Valintatapajono buildValintatapaJono(JonoBuilder j, int prioriteetti) {
        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setAloituspaikat(j.aloituspaikat);
        valintatapajono.setTasasijasaanto(j.tasasijasaanto);
        valintatapajono.setHakemukset(j.hakemukset);
        valintatapajono.setPrioriteetti(prioriteetti);
        valintatapajono.setOid(j.valintatapajonoOid);
        return valintatapajono;
    }

    public SijoitteluajoWrapper buildSijoitteluajoWrapper() {
//        verify();
        SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
        return SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(sijoitteluAjo, Arrays.asList(buildHakukohde()), Arrays.asList());
    }

    private SijoitusAjoBuilder verify() {
        JonoBuilder firstJono = jonot.get(0);
        for (JonoBuilder jono : jonot) {
            assertEquals("Hakija-jonoissa virhe", jono.getHakijaNames(), firstJono.getHakijaNames());
        }
        return this;
    }

    public HashMap forTest() {
        return new HashMap() {{
            List<List<String>> jonoList = jonot.stream().map(j -> hakemuksetStringList(j.hakemukset)).collect(Collectors.toList());
            put("jonot", jonoList);
        }};
    }

    private static List<String> hakemuksetStringList(List<Hakemus> hakemukset) {
        return hakemukset.stream().map(h -> "" +h.getJonosija() + ":" + h.getTasasijaJonosija() + ":" + h.getHakijaOid() + ":" + h.getTila()).collect(Collectors.toList());
    }

    public static HashMap forTest(Hakukohde hakukohde) {
        return new HashMap() {{
            List<List<String>> jonoList = hakukohde.getValintatapajonot().stream().map(j -> hakemuksetStringList(j.getHakemukset())).collect(Collectors.toList());
            put("jonot", jonoList);
        }};
    }

    public class JonoBuilder {
        private final int aloituspaikat;
        private final List<Hakemus> hakemukset = new ArrayList<>();
        private final Tasasijasaanto tasasijasaanto;
        private final String valintatapajonoOid;
        private boolean samaJonoSija = false;
        private int tasasijaJonoSija;
        private int alkuperainenJonosija;

        public JonoBuilder(int aloituspaikat, Tasasijasaanto tasasijasaanto, String valintatapajonoOid) {
            this.aloituspaikat = aloituspaikat;
            this.tasasijasaanto = tasasijasaanto;
            this.valintatapajonoOid = valintatapajonoOid;
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
                if(!samaJonoSija) {
                    tasasijaJonoSija = 1;
                }
                int jonosija = 1 + (samaJonoSija? alkuperainenJonosija : hakemukset.size());
                Hakemus h = createHakemus(hakija, tila, jonosija, tasasijaJonoSija);
                hakemukset.add(h);
                tasasijaJonoSija++;
            }
            return this;
        }

        public JonoBuilder copyHakemusWithTila(Hakemus hakemus, HakemuksenTila tila) {
            hakemukset.add(createHakemus(hakemus.getHakemusOid(), tila, hakemus.getJonosija(), hakemus.getTasasijaJonosija()));
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
