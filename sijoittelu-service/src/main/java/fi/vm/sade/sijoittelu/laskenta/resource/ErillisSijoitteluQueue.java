package fi.vm.sade.sijoittelu.laskenta.resource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ErillisSijoitteluQueue {

    private ErillisSijoitteluQueue() {}

    private static ErillisSijoitteluQueue instance = new ErillisSijoitteluQueue();

    private ConcurrentMap<String,List<Long>> queue = new ConcurrentHashMap<>();
    private AtomicLong ids = new AtomicLong();

    public static ErillisSijoitteluQueue getInstance() {
        return instance;
    }

    public Long queueNewErillissijoittelu(String hakuOid) {
        Long id = ids.getAndIncrement();
        queue.compute(hakuOid,(key, list) -> {
            if(null == list) {
                list = new ArrayList<>();
            }
            if(!list.contains(id)){
                list.add(id);
            }
            return list;
        });
        return id;
    }

    public boolean isOkToStartErillissijoittelu(String hakuOid, Long id) {
        return queue.getOrDefault(hakuOid, new ArrayList<>()).indexOf(id) == 0;
    }

    public boolean erillissijoitteluDone(String hakuOid, Long id) {
        AtomicBoolean removed = new AtomicBoolean(false);
        queue.computeIfPresent(hakuOid, (key, list) -> {
            removed.set(list.remove(id));
            return list;
        });
        return removed.get();
    }
}
