package fi.vm.sade.sijoittelu.tulos.generator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.DefaultEntityCache;

import com.mongodb.DBObject;

import fi.vm.sade.sijoittelu.tulos.testfixtures.MongoMockData;

public class ObjectTemplate {
    private final DBObject templateData;
    private final Map<Class, Object> templates = new HashMap<>();

    public ObjectTemplate(String datafile) {
        templateData = MongoMockData.readJson(datafile);
    }

    public <T extends Serializable> T getTemplate(String collection, Class<T> clazz) {
        @SuppressWarnings("unchecked") T result = (T) templates.get(clazz);
        if (result == null) {
            final DBObject mongoObject = MongoMockData.collectionElements(templateData, collection).get(0);
            result = new Mapper().fromDBObject(null, clazz, mongoObject, new DefaultEntityCache());
            templates.put(clazz, result);
        }
        return SerializationUtils.clone(result);
    }
}
