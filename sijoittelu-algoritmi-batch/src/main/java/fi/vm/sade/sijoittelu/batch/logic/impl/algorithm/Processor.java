package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

public interface Processor {

    default String name() {
        return getClass().getSimpleName();
    }

}
