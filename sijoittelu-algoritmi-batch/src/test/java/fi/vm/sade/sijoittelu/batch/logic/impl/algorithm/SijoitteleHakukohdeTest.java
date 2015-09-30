package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HenkiloWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
        hakemus1.setTila(HakemuksenTila.VARALLA);
        hakemus2.setPrioriteetti(2);
        hakemus2.setTila(HakemuksenTila.PERUNUT);
        hakemus3.setPrioriteetti(3);
        hakemus3.setTila(HakemuksenTila.VARALLA);
        hakemus4.setPrioriteetti(4);
        hakemus4.setTila(HakemuksenTila.VARALLA);


        Assert.assertTrue(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper1));
        //ok koska kakkonen on sama instanssi kuin verrattava kohde
        Assert.assertTrue(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper2));
        Assert.assertFalse(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper3));
        Assert.assertFalse(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper4));
    }
}