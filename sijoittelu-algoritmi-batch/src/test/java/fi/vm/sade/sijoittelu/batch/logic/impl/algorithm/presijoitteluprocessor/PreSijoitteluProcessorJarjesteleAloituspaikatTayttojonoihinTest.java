package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import com.google.common.collect.Lists;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluSilmukkaException;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluTestSpec;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.*;
import static org.junit.Assert.assertEquals;

public class PreSijoitteluProcessorJarjesteleAloituspaikatTayttojonoihinTest extends SijoitteluTestSpec {

    private PreSijoitteluProcessorJarjesteleAloituspaikatTayttojonoihin p;

    public PreSijoitteluProcessorJarjesteleAloituspaikatTayttojonoihinTest() {
        p = new PreSijoitteluProcessorJarjesteleAloituspaikatTayttojonoihin();
    }

    @Test
    public void testMovesAloituspaikkaFromJonoToTayttojono() {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder()
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono1")
                                .withAloituspaikat(1)
                                .withTayttojono("jono2")
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
                                .withAloituspaikat(0)
                                .build())
                .build());

        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);

        assertEquals(1, sijoitteluAjo.getHakukohteet().size());
        assertEquals(2, sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().size());
        List<ValintatapajonoWrapper> vtjs = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot();
        assertEquals(0, vtjs.get(0).getValintatapajono().getAloituspaikat().intValue());
        assertEquals(1, vtjs.get(1).getValintatapajono().getAloituspaikat().intValue());
    }

    @Test
    public void testMovesAloituspaikkaFromJonoToTayttojonosTayttojono() {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder()
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono1")
                                .withAloituspaikat(1)
                                .withTayttojono("jono2")
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
                                .withAloituspaikat(0)
                                .withTayttojono("jono3")
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono3")
                                .withAloituspaikat(0)
                                .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);

        assertEquals(1, sijoitteluAjo.getHakukohteet().size());
        assertEquals(3, sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().size());
        List<ValintatapajonoWrapper> vtjs = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot();
        assertEquals(0, vtjs.get(0).getValintatapajono().getAloituspaikat().intValue());
        assertEquals(0, vtjs.get(1).getValintatapajono().getAloituspaikat().intValue());
        assertEquals(1, vtjs.get(2).getValintatapajono().getAloituspaikat().intValue());
        assertEquals("jono3", vtjs.get(2).getValintatapajono().getOid());
    }

    @Test
    public void testMovesAloituspaikkaFromJonoToTayttojonoIfPartiallyFull() {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder()
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono1")
                                .withAloituspaikat(2)
                                .withTayttojono("jono2")
                                .withHakemus(HYVAKSYTTY)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
                                .withAloituspaikat(0)
                                .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);

        assertEquals(1, sijoitteluAjo.getHakukohteet().size());
        assertEquals(2, sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().size());
        List<ValintatapajonoWrapper> vtjs = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot();
        assertEquals(1, vtjs.get(0).getValintatapajono().getAloituspaikat().intValue());
        assertEquals(1, vtjs.get(1).getValintatapajono().getAloituspaikat().intValue());
    }

    @Test
    public void testMovesAloituspaikkaFromJonoToTayttojonoIfFullButNotQualifying() {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder()
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono1")
                                .withAloituspaikat(2)
                                .withTayttojono("jono2")
                                .withHakemus(HYVAKSYTTY)
                                .withHakemus(PERUNUT)
                                .withHakemus(PERUUTETTU)
                                .withHakemus(PERUUNTUNUT)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
                                .withAloituspaikat(0)
                                .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);

        assertEquals(1, sijoitteluAjo.getHakukohteet().size());
        assertEquals(2, sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().size());
        List<ValintatapajonoWrapper> vtjs = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot();
        assertEquals(4, vtjs.get(0).getValintatapajono().getHakemukset().size());
        assertEquals(0, vtjs.get(1).getValintatapajono().getHakemukset().size());
        assertEquals(1, vtjs.get(0).getValintatapajono().getAloituspaikat().intValue());
        assertEquals(1, vtjs.get(1).getValintatapajono().getAloituspaikat().intValue());
    }

    @Test
    public void testDoesnotMoveAloituspaikkaFromJonoToTayttojono() {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder()
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono1")
                                .withAloituspaikat(2)
                                .withTayttojono("jono2")
                                .withHakemus(VARALLA)
                                .withHakemus(VARASIJALTA_HYVAKSYTTY)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
                                .withAloituspaikat(0)
                                .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1)).build();

        p.process(sijoitteluAjo);

        assertEquals(1, sijoitteluAjo.getHakukohteet().size());
        assertEquals(2, sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().size());
        List<ValintatapajonoWrapper> vtjs = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot();
        assertEquals(2, vtjs.get(0).getValintatapajono().getHakemukset().size());
        assertEquals(0, vtjs.get(1).getValintatapajono().getHakemukset().size());
        assertEquals(2, vtjs.get(0).getValintatapajono().getAloituspaikat().intValue());
        assertEquals(0, vtjs.get(1).getValintatapajono().getAloituspaikat().intValue());
    }

    @Test
    public void testDoesnotMoveAloituspaikkaFromJonoToTayttojono2() {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder()
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono1")
                                .withAloituspaikat(2)
                                .withTayttojono("jono2")
                                .withHakemus(VARALLA)
                                .withHakemus(VARASIJALTA_HYVAKSYTTY)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
                                .withAloituspaikat(0)
                                .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(false).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);

        assertEquals(1, sijoitteluAjo.getHakukohteet().size());
        assertEquals(2, sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().size());
        List<ValintatapajonoWrapper> vtjs = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot();
        assertEquals(2, vtjs.get(0).getValintatapajono().getHakemukset().size());
        assertEquals(0, vtjs.get(1).getValintatapajono().getHakemukset().size());
        assertEquals(2, vtjs.get(0).getValintatapajono().getAloituspaikat().intValue());
        assertEquals(0, vtjs.get(1).getValintatapajono().getAloituspaikat().intValue());
    }

    @Ignore
    @Test(expected = SijoitteluSilmukkaException.class)
    public void testBreaksOnLoop() {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder()
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono")
                                .withAloituspaikat(1)
                                .withTayttojono("jono")
                                .build())

                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);
    }

    @Ignore
    @Test(expected = SijoitteluSilmukkaException.class)
    public void testBreaksOnLongLoop() {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder()
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono1")
                                .withAloituspaikat(1)
                                .withTayttojono("jono2")
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
                                .withAloituspaikat(0)
                                .withTayttojono("jono3")
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono3")
                                .withAloituspaikat(0)
                                .withTayttojono("jono1")
                                .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);
    }

    @Test
    public void testHasCorrectAlkuperaisetAloituspaikat() {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder()
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withAloituspaikat(100)
                                .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
            new SijoitteluConfiguration(), new SijoitteluAjo(), hakukohteet, new ArrayList<>(), Collections.emptyMap());

        p.process(sijoitteluAjo);

        assertEquals(100, sijoitteluAjo
                .getHakukohteet()
                .get(0)
                .getValintatapajonot()
                .get(0)
                .getValintatapajono()
                .getAlkuperaisetAloituspaikat()
                .intValue());
    }

    // =================================
    // Helpers

    private class SijoitteluajoWrapperBuilder {
        private final SijoitteluajoWrapper wrapper;

        SijoitteluajoWrapperBuilder(List<Hakukohde> hakukohteet) {
            this.wrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                new SijoitteluConfiguration(), new SijoitteluAjo(), hakukohteet, new ArrayList<>(), Collections.emptyMap());
        }

        SijoitteluajoWrapperBuilder withVarasijaSaannotAstuvatVoimaan(LocalDateTime varasijaSaannotAstuvatVoimaan) {
            wrapper.setVarasijaSaannotAstuvatVoimaan(varasijaSaannotAstuvatVoimaan);
            return this;
        }

        SijoitteluajoWrapperBuilder withKKHaku(boolean kkHaku) {
            wrapper.setKKHaku(kkHaku);
            return this;
        }

        SijoitteluajoWrapper build() { return wrapper; }

    }

    private class HakukohdeBuilder {
        private final Hakukohde hk;

        HakukohdeBuilder() {
            this.hk = new Hakukohde();
        }

        Hakukohde build() {
            return hk;
        }

        HakukohdeBuilder withValintatapajono(Valintatapajono a) {
            hk.getValintatapajonot().add(a);
            return this;
        }
    }

    private class ValintatapajonoBuilder {
        private final Valintatapajono vtj;

        ValintatapajonoBuilder() {
            this.vtj = new Valintatapajono();
        }

        ValintatapajonoBuilder withOid(String a) {
            vtj.setOid(a);
            return this;
        }

        ValintatapajonoBuilder withTayttojono(String a) {
            vtj.setTayttojono(a);
            return this;
        }

        ValintatapajonoBuilder withAloituspaikat(int a) {
            vtj.setAloituspaikat(a);
            return this;
        }

        ValintatapajonoBuilder withHakemus(HakemuksenTila a) {
            Hakemus h = new Hakemus();
            h.setHakemusOid("oid");
            h.setTila(VARALLA);
            h.setEdellinenTila(a);
            vtj.getHakemukset().add(h);
            return this;
        }

        Valintatapajono build() {
            return vtj;
        }

    }

}
