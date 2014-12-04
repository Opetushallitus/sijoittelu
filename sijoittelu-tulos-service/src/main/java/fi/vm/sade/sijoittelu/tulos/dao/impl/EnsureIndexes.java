package fi.vm.sade.sijoittelu.tulos.dao.impl;

import org.mongodb.morphia.Datastore;

public class EnsureIndexes {
    public static void ensureIndexes(Datastore morphiaDS, Class c) {
        if (!"true".equals(System.getProperty("skipIndexCreation"))) {
            morphiaDS.ensureIndexes(c);
        }
    }
}