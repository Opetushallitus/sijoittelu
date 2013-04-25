package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public final class SijoitteluHelper {

    private SijoitteluHelper() {

    }

    public static <T> T peek(ListIterator<T> it) {
        T item = null;
        if (it.hasNext()) {
            item = it.next();
            it.previous();
        }
        return item;
    }

    public static <T> List<T> nLast(ArrayList<T> korvattavissaOlevat, int i) {
        return korvattavissaOlevat.subList(korvattavissaOlevat.size() - i, korvattavissaOlevat.size());
    }

    public static <T> T last(ArrayList<T> korvattavissaOlevat) {
        return korvattavissaOlevat.get(korvattavissaOlevat.size() - 1);
    }
}
