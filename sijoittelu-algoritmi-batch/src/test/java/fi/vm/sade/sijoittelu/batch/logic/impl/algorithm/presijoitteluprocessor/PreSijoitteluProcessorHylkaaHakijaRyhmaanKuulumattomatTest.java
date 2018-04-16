package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakijaryhma;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomatTest {
    private final Hakukohde kohde1 = mock(Hakukohde.class);
    private final Hakukohde kohde2 = mock(Hakukohde.class);
    private final Hakukohde kohde3 = mock(Hakukohde.class);
    private final HakukohdeWrapper kohde1Wrapper = mock(HakukohdeWrapper.class);
    private final HakukohdeWrapper kohde2Wrapper = mock(HakukohdeWrapper.class);
    private final HakukohdeWrapper kohde3Wrapper = mock(HakukohdeWrapper.class);

    private final Hakijaryhma kohde1Ryhma1 = mock(Hakijaryhma.class);
    private final Hakijaryhma kohde1Ryhma2 = mock(Hakijaryhma.class);
    private final Hakijaryhma kohde2Ryhma1 = mock(Hakijaryhma.class);
    private final Hakijaryhma kohde3Ryhma1 = mock(Hakijaryhma.class);
    private final Hakijaryhma kohde3Ryhma2 = mock(Hakijaryhma.class);

    private final PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat processor = new PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat();
    private SijoitteluajoWrapper sijoitteluajoWrapper = mock(SijoitteluajoWrapper.class);
    private String sijoitteluAjoId = "sijoitteluAjoId";

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() {
        when(sijoitteluajoWrapper.getHakukohteet()).thenReturn(Arrays.asList(kohde1Wrapper, kohde2Wrapper, kohde3Wrapper));
        when(sijoitteluajoWrapper.getSijoitteluAjoId()).thenReturn(sijoitteluAjoId);

        when(kohde1.getOid()).thenReturn("kohde1oid");
        when(kohde2.getOid()).thenReturn("kohde2oid");
        when(kohde3.getOid()).thenReturn("kohde3oid");

        when(kohde1Wrapper.getHakukohde()).thenReturn(kohde1);
        when(kohde2Wrapper.getHakukohde()).thenReturn(kohde2);
        when(kohde3Wrapper.getHakukohde()).thenReturn(kohde3);

        when(kohde1Wrapper.getHakijaryhmaWrappers()).thenReturn(inWrappers(kohde1Ryhma1, kohde1Ryhma2));
        when(kohde3Wrapper.getHakijaryhmaWrappers()).thenReturn(inWrappers(kohde2Ryhma1));
        when(kohde3Wrapper.getHakijaryhmaWrappers()).thenReturn(inWrappers(kohde3Ryhma1, kohde3Ryhma2));

        when(kohde1Ryhma1.getHakukohdeOid()).thenReturn("kohde1Oid");
        when(kohde1Ryhma2.getHakukohdeOid()).thenReturn("kohde1Oid");
        when(kohde2Ryhma1.getHakukohdeOid()).thenReturn("kohde2Oid");
        when(kohde3Ryhma1.getHakukohdeOid()).thenReturn("kohde3Oid");
        when(kohde3Ryhma2.getHakukohdeOid()).thenReturn("kohde3Oid");

        when(kohde1Ryhma1.isKaytaKaikki()).thenReturn(true);
        when(kohde1Ryhma2.isKaytaKaikki()).thenReturn(true);
        when(kohde2Ryhma1.isKaytaKaikki()).thenReturn(false);
        when(kohde3Ryhma1.isKaytaKaikki()).thenReturn(true);
        when(kohde3Ryhma2.isKaytaKaikki()).thenReturn(true);

        when(kohde1Ryhma1.getOid()).thenReturn("kohde1Ryhma1Oid");
        when(kohde1Ryhma1.getNimi()).thenReturn("Ensimmäisen kohteen ensimmäinen ryhmä");
        when(kohde1Ryhma2.getOid()).thenReturn("kohde1Ryhma2Oid");
        when(kohde1Ryhma2.getNimi()).thenReturn("Ensimmäisen kohteen toinen ryhmä");
        when(kohde3Ryhma1.getOid()).thenReturn("kohde3Ryhma1Oid");
        when(kohde3Ryhma1.getNimi()).thenReturn("Kolmannen kohteen ensimmäinen ryhmä");
        when(kohde3Ryhma2.getOid()).thenReturn("kohde3Ryhma2Oid");
        when(kohde3Ryhma2.getNimi()).thenReturn("Kolmannen kohteen toinen ryhmä");
    }

    private List<HakijaryhmaWrapper> inWrappers(Hakijaryhma... ryhmat) {
        return Arrays.stream(ryhmat).map(r -> {
            HakijaryhmaWrapper wrapper = new HakijaryhmaWrapper();
            wrapper.setHakijaryhma(r);
            return wrapper;
        }).collect(Collectors.toList());
    }

    @Test
    public void kaikkiHakukohteetJoissaOnUseampiHakijaryhmaJossaVainRyhmaanKuuluvatHyvaksytaanListataanKerralla() {
        expected.expectMessage("(Sijoitteluajo sijoitteluAjoId) 2 hakukohteelle on olemassa useampi kuin yksi hakijaryhmä, " +
            "josta vain ryhmään kuuluvat olisi tarkoitus hyväksyä. Sijoittelua ei voida suorittaa. Ryhmät: [" +
            "kohde1Ryhma1Oid (\"Ensimmäisen kohteen ensimmäinen ryhmä\"), hakukohdeOid = kohde1Oid , valintatapajonoOid = null ., " +
            "kohde1Ryhma2Oid (\"Ensimmäisen kohteen toinen ryhmä\"), hakukohdeOid = kohde1Oid , valintatapajonoOid = null ., " +
            "kohde3Ryhma1Oid (\"Kolmannen kohteen ensimmäinen ryhmä\"), hakukohdeOid = kohde3Oid , valintatapajonoOid = null ., " +
            "kohde3Ryhma2Oid (\"Kolmannen kohteen toinen ryhmä\"), hakukohdeOid = kohde3Oid , valintatapajonoOid = null .]");
        expected.expect(IllegalStateException.class);
        processor.process(sijoitteluajoWrapper);
    }
}
