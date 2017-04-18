package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.prepostsijoitteluprocessor;

import com.google.common.collect.Lists;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveetTest {

    private final PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet p;

    public PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveetTest() {
        this.p = new PrePostSijoitteluProcessorPeruunnaYlemmatHakutoiveet();
    }

    private void assertHakemuksenTila(String hakemusOid, String vtjOid, HakemuksenTila tila, SijoitteluajoWrapper sw) {
        assertEquals(hakemusOid + " in "+ vtjOid + " should be in state " + tila, true, sw.getHakukohteet().stream()
            .flatMap(hkw -> hkw.getValintatapajonot().stream())
            .map(ValintatapajonoWrapper::getValintatapajono)
            .filter(vtj -> vtj.getOid().equals(vtjOid))
            .flatMap(vtj -> vtj.getHakemukset().stream())
            .anyMatch(h -> h.getHakemusOid().equals(hakemusOid) && h.getTila() == tila));
    }

    private List<Hakukohde> buildHakukohteet() {
        List<Hakukohde> hakukohteet = Lists.newArrayList();
        hakukohteet.add(new HakuBuilder.HakukohdeBuilder("hk1")
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk1_vtj1")
                    .withSivssnov(true)
                    .withHakemus(new HakuBuilder.HakemusBuilder()
                        .withHakijaOid("hakija1")
                        .withOid("hakija1")
                        .withTila(HakemuksenTila.VARALLA)
                        .withPrioriteetti(1)
                        .build())
                    .build())
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk1_vtj2")
                    .withSivssnov(true)
                    .withHakemus(new HakuBuilder.HakemusBuilder()
                        .withHakijaOid("hakija1")
                        .withOid("hakija1")
                        .withTila(HakemuksenTila.HYLATTY)
                        .withPrioriteetti(1)
                        .build())
                    .build())
            .build());
        hakukohteet.add(new HakuBuilder.HakukohdeBuilder("hk2")
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk2_vtj1")
                    .withSivssnov(true)
                    .withHakemus(new HakuBuilder.HakemusBuilder()
                        .withHakijaOid("hakija1")
                        .withOid("hakija1")
                        .withTila(HakemuksenTila.HYVAKSYTTY)
                        .withPrioriteetti(3)
                        .build())
                    .withHakemus(new HakuBuilder.HakemusBuilder()
                        .withHakijaOid("hakija2")
                        .withOid("hakija2")
                        .withTila(HakemuksenTila.VARALLA)
                        .withPrioriteetti(2)
                        .build())
                    .build())
            .build());
        hakukohteet.add(new HakuBuilder.HakukohdeBuilder("hk3")
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk3_vtj1")
                    .withSivssnov(true)
                    .withHakemus(new HakuBuilder.HakemusBuilder()
                        .withHakijaOid("hakija2")
                        .withOid("hakija2")
                        .withTila(HakemuksenTila.HYLATTY)
                        .withPrioriteetti(1)
                        .build())
                    .build())
            .withValintatapajono(
                new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("hk3_vtj2")
                    .withSivssnov(true)
                    .withHakemus(new HakuBuilder.HakemusBuilder()
                        .withHakijaOid("hakija2")
                        .withOid("hakija2")
                        .withTila(HakemuksenTila.VARALLA)
                        .withPrioriteetti(1)
                        .build())
                    .build())
            .build());

        return hakukohteet;
    }

    private SijoitteluajoWrapper buildSijoitteluAjoWrapper(List<Hakukohde> hakukohteet) {
        return new HakuBuilder.SijoitteluajoWrapperBuilder(hakukohteet)
            .withKKHaku(true)
            .withAmkopeHaku(true)
            .withVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1))
            .build();
    }

    @Test
    public void testShouldCancelHigherPriorityHakemusWhenLowerPriorityHakemusIsAccepted() {
        final SijoitteluajoWrapper sw = buildSijoitteluAjoWrapper(buildHakukohteet());
        p.process(sw);

        assertHakemuksenTila("hakija1", "hk1_vtj1", HakemuksenTila.PERUUNTUNUT, sw);
        assertHakemuksenTila("hakija1", "hk1_vtj2", HakemuksenTila.HYLATTY, sw);
        assertHakemuksenTila("hakija1", "hk2_vtj1", HakemuksenTila.HYVAKSYTTY, sw);
    }

    @Test
    public void testShouldNotCancelAnythingWhenNoHakemusIsAccepted() {
        final SijoitteluajoWrapper sw = buildSijoitteluAjoWrapper(buildHakukohteet());
        p.process(sw);

        assertHakemuksenTila("hakija2", "hk3_vtj1", HakemuksenTila.HYLATTY, sw);
        assertHakemuksenTila("hakija2", "hk3_vtj2", HakemuksenTila.VARALLA, sw);
        assertHakemuksenTila("hakija2", "hk2_vtj1", HakemuksenTila.VARALLA, sw);
    }

}
