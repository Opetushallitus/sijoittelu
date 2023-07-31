package fi.vm.sade.sijoittelu;

import fi.vm.sade.sijoittelu.laskenta.resource.ErillisSijoitteluQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        Assertions.assertTrue(queue.isOkToStartErillissijoittelu(haku1, id1));
        Assertions.assertFalse(queue.isOkToStartErillissijoittelu(haku1, id2));
        Assertions.assertFalse(queue.isOkToStartErillissijoittelu(haku1, id3));

        Assertions.assertTrue(queue.isOkToStartErillissijoittelu(haku2, id4));

        Assertions.assertTrue(queue.erillissijoitteluDone(haku1, id1));

        Assertions.assertFalse(queue.isOkToStartErillissijoittelu(haku1, id3));
        Assertions.assertTrue(queue.isOkToStartErillissijoittelu(haku1, id2));

        long id5 = queue.queueNewErillissijoittelu(haku2);
        Assertions.assertFalse(queue.isOkToStartErillissijoittelu(haku2, id5));

        Assertions.assertTrue(queue.erillissijoitteluDone(haku1, id2));
        Assertions.assertFalse(queue.erillissijoitteluDone(haku1, id2));

        Assertions.assertTrue(queue.isOkToStartErillissijoittelu(haku1, id3));
        Assertions.assertFalse(queue.isOkToStartErillissijoittelu(haku2, id5));
        Assertions.assertTrue(queue.erillissijoitteluDone(haku1, id3));

        long id6 = queue.queueNewErillissijoittelu(haku1);
        Assertions.assertTrue(queue.isOkToStartErillissijoittelu(haku1, id6));
        Assertions.assertFalse(queue.isOkToStartErillissijoittelu(haku2, id6));
        Assertions.assertFalse(queue.isOkToStartErillissijoittelu(haku1, id1));
    }
}
