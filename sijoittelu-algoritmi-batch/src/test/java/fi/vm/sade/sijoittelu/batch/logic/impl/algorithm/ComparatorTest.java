package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakijaryhmaWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakukohdeWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.ValintatapajonoWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakijaryhma;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class ComparatorTest {

    @Test
    public void testHakukohdeComparator() {
        ArrayList<HakukohdeWrapper> ts = new ArrayList<HakukohdeWrapper>();

        Hakukohde h1 = new Hakukohde();
        HakukohdeWrapper w1 = new HakukohdeWrapper();
        w1.setHakukohde(h1);

        Hakukohde h2 = new Hakukohde();
        HakukohdeWrapper w2 = new HakukohdeWrapper();
        w2.setHakukohde(h2);

        Hakukohde h3 = new Hakukohde();
        HakukohdeWrapper w3 = new HakukohdeWrapper();
        w3.setHakukohde(h3);

        Hakukohde h4 = new Hakukohde();
        HakukohdeWrapper w4 = new HakukohdeWrapper();
        w4.setHakukohde(h4);

        h1.setOid("10.11.100002");
        h2.setOid("10.11.100001)");
        h3.setOid("10.11.100004");
        h4.setOid("10.11.100003");

        ts.add(w1);
        ts.add(w2);
        ts.add(w3);
        ts.add(w4);

        Collections.sort(ts, new HakukohdeWrapperComparator());

        Iterator<HakukohdeWrapper> it = ts.iterator();

        Assert.assertSame(w2, it.next());
        Assert.assertSame(w1, it.next());
        Assert.assertSame(w4, it.next());
        Assert.assertSame(w3, it.next());

    }

    @Test
    public void testHakemusComparator() {
        ArrayList<HakemusWrapper> ts = new ArrayList<HakemusWrapper>();

        Hakemus h1 = new Hakemus();
        HakemusWrapper w1 = new HakemusWrapper();
        w1.setHakemus(h1);

        Hakemus h2 = new Hakemus();
        HakemusWrapper w2 = new HakemusWrapper();
        w2.setHakemus(h2);

        Hakemus h3 = new Hakemus();
        HakemusWrapper w3 = new HakemusWrapper();
        w3.setHakemus(h3);

        Hakemus h4 = new Hakemus();
        HakemusWrapper w4 = new HakemusWrapper();
        w4.setHakemus(h4);

        h1.setJonosija(2);
        h2.setJonosija(1);
        h3.setJonosija(4);
        h4.setJonosija(3);

        ts.add(w1);
        ts.add(w2);
        ts.add(w3);
        ts.add(w4);

        Collections.sort(ts, new HakemusWrapperComparator());

        Iterator<HakemusWrapper> it = ts.iterator();

        Assert.assertSame(w2, it.next());
        Assert.assertSame(w1, it.next());
        Assert.assertSame(w4, it.next());
        Assert.assertSame(w3, it.next());

    }

    @Test
    public void testHakemusComparatorTasasija() {
        ArrayList<HakemusWrapper> ts = new ArrayList<HakemusWrapper>();

        Hakemus h1 = new Hakemus();
        HakemusWrapper w1 = new HakemusWrapper();
        w1.setHakemus(h1);

        Hakemus h11 = new Hakemus();
        HakemusWrapper w11 = new HakemusWrapper();
        w11.setHakemus(h11);

        Hakemus h12 = new Hakemus();
        HakemusWrapper w12 = new HakemusWrapper();
        w12.setHakemus(h12);

        Hakemus h2 = new Hakemus();
        HakemusWrapper w2 = new HakemusWrapper();
        w2.setHakemus(h2);

        Hakemus h21 = new Hakemus();
        HakemusWrapper w21 = new HakemusWrapper();
        w21.setHakemus(h21);

        Hakemus h3 = new Hakemus();
        HakemusWrapper w3 = new HakemusWrapper();
        w3.setHakemus(h3);

        Hakemus h31 = new Hakemus();
        HakemusWrapper w31 = new HakemusWrapper();
        w31.setHakemus(h31);

        Hakemus h32 = new Hakemus();
        HakemusWrapper w32 = new HakemusWrapper();
        w32.setHakemus(h32);

        Hakemus h33 = new Hakemus();
        HakemusWrapper w33 = new HakemusWrapper();
        w33.setHakemus(h33);

        Hakemus h34 = new Hakemus();
        HakemusWrapper w34 = new HakemusWrapper();
        w34.setHakemus(h34);

        Hakemus h4 = new Hakemus();
        HakemusWrapper w4 = new HakemusWrapper();
        w4.setHakemus(h4);

        h1.setJonosija(1);
        h1.setTasasijaJonosija(1);
        h11.setJonosija(1);
        h11.setTasasijaJonosija(2);
        h12.setJonosija(1);
        h12.setTasasijaJonosija(3);

        h2.setJonosija(2);
        h2.setTasasijaJonosija(1);
        h21.setJonosija(2);
        h21.setTasasijaJonosija(2);

        h3.setJonosija(3);
        h3.setTasasijaJonosija(null);
        h31.setJonosija(3);
        h31.setTasasijaJonosija(1);
        h32.setJonosija(3);
        h32.setTasasijaJonosija(null);
        h33.setJonosija(3);
        h33.setTasasijaJonosija(3);
        h34.setJonosija(3);
        h34.setTasasijaJonosija(null);

        h4.setJonosija(4);

        ts.add(w4);
        ts.add(w31);
        ts.add(w1);
        ts.add(w21);
        ts.add(w32);
        ts.add(w2);
        ts.add(w33);
        ts.add(w11);
        ts.add(w34);
        ts.add(w12);
        ts.add(w3);

        Collections.sort(ts, new HakemusWrapperComparator());

        // for (Hakemus h : ts) {
        // System.out.println(h.getJonosija() + "." + h.getTasasijaJonosija());
        // }

        Iterator<HakemusWrapper> it = ts.iterator();

        Assert.assertSame(w1, it.next());
        Assert.assertSame(w11, it.next());
        Assert.assertSame(w12, it.next());
        Assert.assertSame(w2, it.next());
        Assert.assertSame(w21, it.next());
        Assert.assertSame(w31, it.next());
        Assert.assertSame(w33, it.next());
        Assert.assertSame(w4, ts.get(ts.size() - 1));

    }

    @Test
    public void testValintatapajonoComparator() {
        ArrayList<ValintatapajonoWrapper> ts = new ArrayList<ValintatapajonoWrapper>();

        Valintatapajono h1 = new Valintatapajono();
        ValintatapajonoWrapper w1 = new ValintatapajonoWrapper();
        w1.setValintatapajono(h1);

        Valintatapajono h2 = new Valintatapajono();
        ValintatapajonoWrapper w2 = new ValintatapajonoWrapper();
        w2.setValintatapajono(h2);

        Valintatapajono h3 = new Valintatapajono();
        ValintatapajonoWrapper w3 = new ValintatapajonoWrapper();
        w3.setValintatapajono(h3);

        Valintatapajono h4 = new Valintatapajono();
        ValintatapajonoWrapper w4 = new ValintatapajonoWrapper();
        w4.setValintatapajono(h4);

        h1.setPrioriteetti(2);
        h2.setPrioriteetti(1);
        h3.setPrioriteetti(4);
        h4.setPrioriteetti(3);

        ts.add(w1);
        ts.add(w2);
        ts.add(w3);
        ts.add(w4);

        Collections.sort(ts, new ValintatapajonoWrapperComparator());

        Iterator<ValintatapajonoWrapper> it = ts.iterator();

        Assert.assertSame(w2, it.next());
        Assert.assertSame(w1, it.next());
        Assert.assertSame(w4, it.next());
        Assert.assertSame(w3, it.next());

    }

    @Test
    public void testHakijaryhmaWrapperComparator() {

        ArrayList<HakijaryhmaWrapper> ts = new ArrayList<HakijaryhmaWrapper>();

        Hakijaryhma h1 = new Hakijaryhma();
        HakijaryhmaWrapper w1 = new HakijaryhmaWrapper();
        w1.setHakijaryhma(h1);

        Hakijaryhma h2 = new Hakijaryhma();
        HakijaryhmaWrapper w2 = new HakijaryhmaWrapper();
        w2.setHakijaryhma(h2);

        Hakijaryhma h3 = new Hakijaryhma();
        HakijaryhmaWrapper w3 = new HakijaryhmaWrapper();
        w3.setHakijaryhma(h3);

        Hakijaryhma h4 = new Hakijaryhma();
        HakijaryhmaWrapper w4 = new HakijaryhmaWrapper();
        w4.setHakijaryhma(h4);

        h1.setPrioriteetti(2);
        h2.setPrioriteetti(1);
        h3.setPrioriteetti(4);
        h4.setPrioriteetti(3);

        ts.add(w1);
        ts.add(w2);
        ts.add(w3);
        ts.add(w4);

        Collections.sort(ts, new HakijaryhmaWrapperComparator());

        Iterator<HakijaryhmaWrapper> it = ts.iterator();

        Assert.assertSame(w2, it.next());
        Assert.assertSame(w1, it.next());
        Assert.assertSame(w4, it.next());
        Assert.assertSame(w3, it.next());

    }

}
