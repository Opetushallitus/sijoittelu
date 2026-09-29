package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MyohastynytVastaanottoEiSaaOhittaaAitoaVastaanottoaTest {

    @Test
    public void aitoVastaanottoEiJohdaMyohastyneeksiMerkitsemiseen() {
        Hakemus hakemus = new HakuBuilder.HakemusBuilder().withOid("hakemus1").withHakijaOid("hakija1")
                .withPrioriteetti(0).withJonosija(1)
                .withTila(HakemuksenTila.VARALLA).withEdellinenTila(HakemuksenTila.VARALLA)
                .build();
        hakemus.setVastaanottoMyohassa(true);

        Valintatapajono jono = new HakuBuilder.ValintatapajonoBuilder()
                .withOid("jono1").withPrioriteetti(0).withAloituspaikat(1)
                .withHakemus(hakemus)
                .build();
        Hakukohde hakukohde = new HakuBuilder.HakukohdeBuilder("hakukohde1").withValintatapajono(jono).build();

        Valintatulos valintatulos = new Valintatulos("jono1", "hakemus1", "hakukohde1", "hakija1", "haku1", 1);
        valintatulos.setJulkaistavissa(true, "");
        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        valintatulos.setHyvaksyttyVarasijalta(true, "");

        SijoitteluajoWrapper wrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                new SijoitteluConfiguration(), new SijoitteluAjo(), List.of(hakukohde), Collections.emptyMap());
        wrapper.paivitaVastaanottojenVaikutusHakemustenTiloihin(List.of(valintatulos), Collections.emptyMap());
        wrapper.getHakukohteet().get(0).getValintatapajonot().get(0).setMerkitseMyohAuto(true);

        SijoitteluAlgorithmUtil.sijoittele(wrapper);

        assertEquals(HakemuksenTila.VARASIJALTA_HYVAKSYTTY, hakemus.getTila());
        assertNotEquals(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, valintatulos.getTila());
    }
}
