package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.*;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.ARVONTA;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.YLITAYTTO;
import static org.junit.Assert.*;

public class PostSijoitteluProcessorPeruunnutaYlijaaneetVarallaTilaisetJosVarasijatayttoOnPaattynytTest {
    private final PostSijoitteluProcessorPeruunnutaYlijaaneetVarallaTilaisetJosVarasijatayttoOnPaattynyt p = new PostSijoitteluProcessorPeruunnutaYlijaaneetVarallaTilaisetJosVarasijatayttoOnPaattynyt();

    private final Date nytMiinusKolmePaivaa = Date.from(LocalDateTime.now().minusDays(3L).atZone(ZoneId.systemDefault()).toInstant());
    private final Date nytPlusKaksiPaivaa = Date.from(LocalDateTime.now().plusDays(2L).atZone(ZoneId.systemDefault()).toInstant());

    private final Hakemus hakemus1 = new HakuBuilder.HakemusBuilder().withOid("hakemus1")
            .withJonosija(1).withTila(HYVAKSYTTY).withPrioriteetti(1).build();
    private final Hakemus hakemus2 = new HakuBuilder.HakemusBuilder().withOid("hakemus2")
            .withJonosija(2).withTila(HYVAKSYTTY).withPrioriteetti(3).build();
    private final Hakemus hakemus3 = new HakuBuilder.HakemusBuilder().withOid("hakemus3")
            .withJonosija(3).withTila(VARALLA).withPrioriteetti(2).build();
    private final Hakemus hakemus4 = new HakuBuilder.HakemusBuilder().withOid("hakemus4")
            .withJonosija(4).withTila(VARALLA).withPrioriteetti(1).build();

    private final Hakemus hakemus11 = new HakuBuilder.HakemusBuilder().withOid("hakemus11")
            .withJonosija(1).withTila(HYVAKSYTTY).withPrioriteetti(1).build();
    private final Hakemus hakemus12 = new HakuBuilder.HakemusBuilder().withOid("hakemus12")
            .withJonosija(2).withTila(VARASIJALTA_HYVAKSYTTY).withPrioriteetti(3).build();
    private final Hakemus hakemus13 = new HakuBuilder.HakemusBuilder().withOid("hakemus13")
            .withJonosija(3).withTila(VARALLA).withPrioriteetti(2).build();
    private final Hakemus hakemus14 = new HakuBuilder.HakemusBuilder().withOid("hakemus14")
            .withJonosija(4).withTila(VARALLA).withPrioriteetti(1).build();

    private Valintatapajono jono = new HakuBuilder.ValintatapajonoBuilder().withOid("jono1")
            .withTasasijasaanto(YLITAYTTO)
            .withAloituspaikat(2)
            .withPrioriteetti(0)
            .withHakemukset(hakemus1, hakemus2, hakemus3, hakemus4)
            .withSivssnov(true)
            .build();

    private Valintatapajono jono2 = new HakuBuilder.ValintatapajonoBuilder().withOid("jono2")
            .withTasasijasaanto(ARVONTA)
            .withAloituspaikat(2)
            .withPrioriteetti(0)
            .withHakemukset(hakemus11, hakemus12, hakemus13, hakemus14)
            .withSivssnov(false)
            .build();

    private SijoitteluajoWrapper sijoitteluajoWrapper;

    @Before
    public void valmisteleSijoitteluajoWrapper() {
        Hakukohde hakukohde = new HakuBuilder.HakukohdeBuilder("hk1").withValintatapajono(jono).withValintatapajono(jono2).build();
        sijoitteluajoWrapper = new HakuBuilder.SijoitteluajoWrapperBuilder(Collections.singletonList(hakukohde)).build();
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(2));
        assertTrue(sijoitteluajoWrapper.varasijaSaannotVoimassa());
    }

    @Test
    public void testYlijaanytVarallaTilainenHakemusMuutetaanPeruuntuneeksiJosVarasijasaannotVoimassaJaVarasijatayttoPaattynyt() throws Exception {
        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true);
        jono2.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true);

        jono.setVarasijojaTaytetaanAsti(nytMiinusKolmePaivaa);
        jono2.setVarasijojaTaytetaanAsti(nytPlusKaksiPaivaa);

        p.process(sijoitteluajoWrapper);

        assertTrue(PERUUNTUNUT.equals(hakemus3.getTila()));
        assertTrue(PERUUNTUNUT.equals(hakemus4.getTila()));

        assertTrue(VARALLA.equals(hakemus13.getTila()));
        assertTrue(VARALLA.equals(hakemus14.getTila()));

    }

    @Test
    public void testVarallaTilaisiaHakemuksiaEiPeruunnutetaJosJononVarasijatayttoEiOleVoimassa() throws Exception {
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusHours(8));
        sijoitteluajoWrapper.setVarasijaTayttoPaattyy(LocalDateTime.now().plusDays(3));

        p.process(sijoitteluajoWrapper);

        assertTrue(VARALLA.equals(hakemus3.getTila()));
        assertTrue(VARALLA.equals(hakemus4.getTila()));

        assertTrue(VARALLA.equals(hakemus13.getTila()));
        assertTrue(VARALLA.equals(hakemus14.getTila()));
    }
}