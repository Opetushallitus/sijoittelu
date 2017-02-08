package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import com.google.common.collect.Lists;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluTestSpec;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.*;


public class PreSijoitteluProcessorAsetaSivssnovTest extends SijoitteluTestSpec {

    private PreSijoitteluProcessorAsetaSivssnov p;

    public PreSijoitteluProcessorAsetaSivssnovTest() {
        p = new PreSijoitteluProcessorAsetaSivssnov();
    }

    private void assertJonoSivssnov(String name, Boolean assertion, SijoitteluajoWrapper sijoitteluAjo) {
        assertEquals(name + " should have " + assertion + " sivssnov", true, sijoitteluAjo.getHakukohteet().stream()
                .flatMap(hkw -> hkw.getValintatapajonot().stream())
                .anyMatch(vtj ->
                        vtj.getValintatapajono().getOid().equals(name)
                                && vtj.getValintatapajono().getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa() == assertion));
    }

    @Test
    public void testShouldNotProcessWhenVarasijasaannotEiVoimassa()  {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder("hk1")
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono1")
                                .withAloituspaikat(3)
                                .withSivssnov(true)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
                                .withAloituspaikat(2)
                                .withSivssnov(false)
                                .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1)).build();

        p.process(sijoitteluAjo);

        assertJonoSivssnov("jono1", true, sijoitteluAjo);
        assertJonoSivssnov("jono2", false, sijoitteluAjo);
    }

    @Test
    public void testMissingFlagfShouldSetAllFlagsToFalse()  {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder("hk1")
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono1")
                                .withAloituspaikat(3)
                                .withSivssnov(true)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                        .withOid("jono2")
                        .withAloituspaikat(2)
                        .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withAmkopeHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);

        assertEquals("All valintatapajonot should have false sivssnov", true, sijoitteluAjo.getHakukohteet().stream()
                .flatMap(hkw -> hkw.getValintatapajonot().stream())
                .noneMatch(vtj -> vtj.getValintatapajono().getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()));
    }

    @Test
    public void testTrueFlagsfShouldRemainTrue()  {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder("hk1")
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono1")
                                .withAloituspaikat(3)
                                .withSivssnov(true)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
                                .withAloituspaikat(2)
                                .withSivssnov(true)
                                .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withAmkopeHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1)).build();

        p.process(sijoitteluAjo);

        assertEquals("All valintatapajonot should have true sivssnov", true, sijoitteluAjo.getHakukohteet().stream()
                .flatMap(hkw -> hkw.getValintatapajonot().stream())
                .allMatch(vtj -> vtj.getValintatapajono().getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()));
    }

    @Test
    public void testShouldProcessOnlyAmkopeHakus()  {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakukohdeBuilder("hk1")
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono1")
                                .withAloituspaikat(3)
                                .withSivssnov(true)
                                .withPrioriteetti(1)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
                                .withAloituspaikat(2)
                                .withPrioriteetti(2)
                                .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);

        assertJonoSivssnov("jono1", true, sijoitteluAjo);
        assertJonoSivssnov("jono2", null, sijoitteluAjo);
    }

    private class SijoitteluajoWrapperBuilder {
        private final SijoitteluajoWrapper wrapper;

        SijoitteluajoWrapperBuilder(List<Hakukohde> hakukohteet) {
            this.wrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                    new SijoitteluAjo(), hakukohteet, new ArrayList<>(), Collections.emptyMap());
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

        SijoitteluajoWrapperBuilder withAmkopeHaku(boolean b) {
            wrapper.setAmkopeHaku(b);
            return this;
        }
    }

    private class HakukohdeBuilder {
        private final Hakukohde hk;

        HakukohdeBuilder(String oid) {
            this.hk = new Hakukohde();
            this.hk.setOid(oid);
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

        ValintatapajonoBuilder withHakemus(HakemuksenTila a, String oid) {
            this.withHakemus(a).withOid(oid);
            return this;
        }

        ValintatapajonoBuilder withSivssnov(Boolean sivssnov) {
            vtj.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(sivssnov);
            return this;
        }

        ValintatapajonoBuilder withPrioriteetti(int prioriteetti) {
            vtj.setPrioriteetti(prioriteetti);
            return this;
        }

        Valintatapajono build() {
            return vtj;
        }

    }
}