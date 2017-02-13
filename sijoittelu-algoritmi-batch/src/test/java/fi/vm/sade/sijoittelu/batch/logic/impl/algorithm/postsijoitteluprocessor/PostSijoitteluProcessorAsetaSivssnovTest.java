package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import com.google.common.collect.Lists;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;


public class PostSijoitteluProcessorAsetaSivssnovTest {

    private PostSijoitteluProcessorAsetaSivssnov p;

    public PostSijoitteluProcessorAsetaSivssnovTest() {
        p = new PostSijoitteluProcessorAsetaSivssnov();
    }

    private void assertJonoSivssnov(String name, Boolean assertion, SijoitteluajoWrapper sijoitteluAjo) {
        assertEquals(name + " should have " + assertion + " sivssnov", true, sijoitteluAjo.getHakukohteet().stream()
            .flatMap(hkw -> hkw.getValintatapajonot().stream())
            .anyMatch(vtj ->
                vtj.getValintatapajono().getOid().equals(name)
                    && vtj.getValintatapajono().getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa() == assertion));
    }

    @Test
    public void testShouldSetAllFlagsToTrue()  {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakuBuilder.HakukohdeBuilder("hk1")
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk1_jono1")
                    .withSivssnov(true)
                    .build())
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk1_jono2")
                    .withSivssnov(false)
                    .build())
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk1_jono3")
                    .build())
            .build());
        hakukohteet.add(new HakuBuilder.HakukohdeBuilder("hk2")
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk2_jono1")
                    .withSivssnov(false)
                    .build())
            .build());
        final SijoitteluajoWrapper sijoitteluAjo = new HakuBuilder.SijoitteluajoWrapperBuilder(hakukohteet)
            .withKKHaku(true).withAmkopeHaku(true).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);

        assertEquals("All valintatapajonot should have true sivssnov", true, sijoitteluAjo.getHakukohteet().stream()
            .flatMap(hkw -> hkw.getValintatapajonot().stream())
            .allMatch(vtj -> vtj.getValintatapajono().getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()));
    }

    @Test
    public void testShouldOnlyProcessAmkopeHakus()  {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakuBuilder.HakukohdeBuilder("hk1")
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk1_jono1")
                    .withSivssnov(true)
                    .build())
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk1_jono2")
                    .withSivssnov(false)
                    .build())
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk1_jono3")
                    .build())
            .build());
        hakukohteet.add(new HakuBuilder.HakukohdeBuilder("hk2")
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk2_jono1")
                    .withSivssnov(false)
                    .build())
            .build());
        final SijoitteluajoWrapper sijoitteluAjo = new HakuBuilder.SijoitteluajoWrapperBuilder(hakukohteet)
            .withKKHaku(true).withAmkopeHaku(false).withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1)).build();

        p.process(sijoitteluAjo);

        assertJonoSivssnov("hk1_jono1", true, sijoitteluAjo);
        assertJonoSivssnov("hk1_jono2", false, sijoitteluAjo);
        assertJonoSivssnov("hk1_jono3", false, sijoitteluAjo);
        assertJonoSivssnov("hk2_jono1", false, sijoitteluAjo);
    }
}