package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import com.google.common.collect.Lists;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluTestSpec;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.*;


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
                                .withSivssnov(true)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
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
                                .withSivssnov(true)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                        .withOid("jono2")
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
                                .withSivssnov(true)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
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
                                .withSivssnov(true)
                                .build())
                .withValintatapajono(
                        new ValintatapajonoBuilder()
                                .withOid("jono2")
                                .build())
                .build());
        final SijoitteluajoWrapper sijoitteluAjo = new SijoitteluajoWrapperBuilder(hakukohteet)
                .withKKHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);

        assertJonoSivssnov("jono1", true, sijoitteluAjo);
        assertJonoSivssnov("jono2", false, sijoitteluAjo);
    }
}