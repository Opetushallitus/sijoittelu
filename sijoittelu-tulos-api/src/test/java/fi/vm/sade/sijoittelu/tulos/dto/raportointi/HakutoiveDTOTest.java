package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveDTO;
import org.junit.Test;

import java.util.Arrays;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HakutoiveDTOTest {

    private HakutoiveDTO createHakutoive(Integer priority, String oid) {
        HakutoiveDTO toive = new HakutoiveDTO();
        toive.setHakutoive(priority);
        toive.setHakukohdeOid(oid);
        return toive;
    }

    private void assertSameOrder(HakutoiveDTO o1, HakutoiveDTO o2) {
        assertEquals(0, o1.compareTo(o2));
        assertEquals(0, o2.compareTo(o1));
    }

    private void assertFirstIsBefore(HakutoiveDTO o1, HakutoiveDTO o2) {
        int diff = o1.compareTo(o2);
        assertTrue("diff " + diff + " is not negative", diff < 0);
        assertEquals(-diff, o2.compareTo(o1));
    }

    @Test
    public void testCompareEmptyObjects() {
        HakutoiveDTO o1 = createHakutoive(null, null);
        HakutoiveDTO o2 = createHakutoive(null, null);
        assertSameOrder(o1, o2);
    }

    @Test
    public void testWithDifferentPriority() {
        HakutoiveDTO o1 = createHakutoive(1, null);
        HakutoiveDTO o2 = createHakutoive(2, null);
        assertFirstIsBefore(o1, o2);
    }

    @Test
    public void testWithSamePriorityAndSameOid() {
        HakutoiveDTO o1 = createHakutoive(1, "oid1");
        HakutoiveDTO o2 = createHakutoive(1, "oid1");
        assertSameOrder(o1, o2);
    }

    @Test
    public void testWithSamePriorityDifferentOid() {
        HakutoiveDTO o1 = createHakutoive(1, "oid1");
        HakutoiveDTO o2 = createHakutoive(1, "oid2");
        assertFirstIsBefore(o1, o2);
    }

    @Test
    public void testWithNoPriorityButDifferentOid() {
        HakutoiveDTO o1 = createHakutoive(null, "oid1");
        HakutoiveDTO o2 = createHakutoive(null, null);
        assertFirstIsBefore(o1, o2);
    }

    @Test
    public void testWithSamePriorityAndOtherWithOtherNOOid() {
        HakutoiveDTO o1 = createHakutoive(1, "oid1");
        HakutoiveDTO o2 = createHakutoive(1, null);
        assertFirstIsBefore(o1, o2);
    }

    @Test
    public void testCompareWithPriorityAndEmptyObject() {
        HakutoiveDTO o1 = createHakutoive(1, null);
        HakutoiveDTO o2 = createHakutoive(null, null);
        assertFirstIsBefore(o1, o2);
    }

    @Test
    public void testSorting() {
        HakutoiveDTO o1 = createHakutoive(3, "oid3");
        HakutoiveDTO o2 = createHakutoive(1, "oid1");
        HakutoiveDTO o3 = createHakutoive(2, null);
        HakutoiveDTO o4 = createHakutoive(null, "oid4");
        HakutoiveDTO o5 = createHakutoive(1, "oid3");
        HakutoiveDTO o6 = createHakutoive(2, null);
        HakutoiveDTO o7 = createHakutoive(null, null);
        assertEquals(new TreeSet(Arrays.asList(o2, o5, o6, o1, o4, o7)), new TreeSet(Arrays.asList(o1, o2, o3, o4, o5, o6, o7)));
    }
}
