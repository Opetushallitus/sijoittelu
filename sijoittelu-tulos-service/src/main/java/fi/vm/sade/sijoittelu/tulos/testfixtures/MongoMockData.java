package fi.vm.sade.sijoittelu.tulos.testfixtures;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import com.mongodb.*;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.mongodb.util.JSON;

public class MongoMockData {
    public static List<DBObject> collectionElements(DBObject data, String collection) {
        final List objects = Arrays.asList(((BasicDBList) data.get(collection)).toArray());
        return (List<DBObject>) objects;
    }

    public static void insertData(DB db, DBObject data) {
        for (String collection : data.keySet()) {
            BasicDBList collectionData = (BasicDBList) data.get(collection);
            DBCollection c = db.getCollection(collection);
            for (Object dataObject : collectionData) {
                DBObject dbObject = (DBObject) dataObject;
                final Object id = dbObject.get("_id");
                c.update(new BasicDBObject("_id", id), dbObject, true, false);
            }
        }
    }

    public static DBObject readJson(String path) {
        Resource r = new ClassPathResource(path);
        StringWriter w = new StringWriter();
        try {
            IOUtils.copy(r.getInputStream(), w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return (DBObject) JSON.parse(w.toString());
    }
}
