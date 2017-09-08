package fi.vm.sade.sijoittelu;

import fi.vm.sade.sijoittelu.laskenta.resource.ErillisSijoitteluQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ErillisSijoitteluQueueTest {

    @Test
    public void testErillisSijoitteluQueue() {
        ErillisSijoitteluQueue queue = ErillisSijoitteluQueue.getInstance();

        String haku1 = "hakuOid1";
        String haku2 = "hakuOid2";
        long id1 = queue.queueNewErillissijoittelu(haku1);
        long id2 = queue.queueNewErillissijoittelu(haku1);
        long id3 = queue.queueNewErillissijoittelu(haku1);
        long id4 = queue.queueNewErillissijoittelu(haku2);

        assertTrue(queue.isOkToStartErillissijoittelu(haku1, id1));
        assertFalse(queue.isOkToStartErillissijoittelu(haku1, id2));
        assertFalse(queue.isOkToStartErillissijoittelu(haku1, id3));

        assertTrue(queue.isOkToStartErillissijoittelu(haku2, id4));

        queue.erillissijoitteluDone(haku1, id1);

        assertFalse(queue.isOkToStartErillissijoittelu(haku1, id3));
        assertTrue(queue.isOkToStartErillissijoittelu(haku1, id2));

        long id5 = queue.queueNewErillissijoittelu(haku2);
        assertFalse(queue.isOkToStartErillissijoittelu(haku2, id5));

        queue.erillissijoitteluDone(haku1, id2);

        assertTrue(queue.isOkToStartErillissijoittelu(haku1, id3));
        assertFalse(queue.isOkToStartErillissijoittelu(haku2, id5));
        queue.erillissijoitteluDone(haku1, id3);

        long id6 = queue.queueNewErillissijoittelu(haku1);
        assertTrue(queue.isOkToStartErillissijoittelu(haku1, id6));
        assertFalse(queue.isOkToStartErillissijoittelu(haku2, id6));
        assertFalse(queue.isOkToStartErillissijoittelu(haku1, id1));
    }
}
