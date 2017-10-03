package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import static fi.vm.sade.sijoittelu.SijoitteluMatchers.hasTila;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYVAKSYTTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.PERUNUT;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.PERUUNTUNUT;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.ARVONTA;
import static org.junit.Assert.assertThat;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakemusBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakukohdeBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.ValintatapajonoBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HenkiloWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class SijoitteleHakukohdeTest {

    @Test
    public void eiPeruttuaKorkeampaaTaiSamaaHakutoivettaTest() {

        HenkiloWrapper henkiloWrapper = new HenkiloWrapper();

        HakemusWrapper hakemusWrapper1 = new HakemusWrapper();
        HakemusWrapper hakemusWrapper2 = new HakemusWrapper();
        HakemusWrapper hakemusWrapper3 = new HakemusWrapper();
        HakemusWrapper hakemusWrapper4 = new HakemusWrapper();
        Hakemus hakemus1 = new Hakemus();
        Hakemus hakemus2 = new Hakemus();
        Hakemus hakemus3 = new Hakemus();
        Hakemus hakemus4 = new Hakemus();
        hakemusWrapper1.setHakemus(hakemus1);
        hakemusWrapper2.setHakemus(hakemus2);
        hakemusWrapper3.setHakemus(hakemus3);
        hakemusWrapper4.setHakemus(hakemus4);

        henkiloWrapper.getHakemukset().add(hakemusWrapper1);
        henkiloWrapper.getHakemukset().add(hakemusWrapper2);
        henkiloWrapper.getHakemukset().add(hakemusWrapper3);
        henkiloWrapper.getHakemukset().add(hakemusWrapper4);
        hakemusWrapper1.setHenkilo(henkiloWrapper);
        hakemusWrapper2.setHenkilo(henkiloWrapper);
        hakemusWrapper3.setHenkilo(henkiloWrapper);
        hakemusWrapper4.setHenkilo(henkiloWrapper);


        hakemus1.setPrioriteetti(1);
        hakemus1.setTila(VARALLA);
        hakemus2.setPrioriteetti(2);
        hakemus2.setTila(PERUNUT);
        hakemus3.setPrioriteetti(3);
        hakemus3.setTila(VARALLA);
        hakemus4.setPrioriteetti(4);
        hakemus4.setTila(VARALLA);


        Assert.assertTrue(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper1));
        //ok koska kakkonen on sama instanssi kuin verrattava kohde
        Assert.assertTrue(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper2));
        Assert.assertFalse(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper3));
        Assert.assertFalse(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper4));
    }
}