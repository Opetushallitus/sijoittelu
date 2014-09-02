package fi.vm.sade.sijoittelu.tulos.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
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
                c.insert((DBObject) dataObject);
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
