package fi.vm.sade.sijoittelu.tulos.service;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringWriter;

@Service
@Profile("it")
public class DataImportService {

    @Autowired
    MongoClient mongo;

    @Value("${sijoittelu-service.mongodb.dbname}")
    String dbname;

    @PostConstruct
    public void importData() throws IOException {
        DB db = mongo.getDB(dbname);
        DBObject data = readJson();
        for (String collection : data.keySet()) {
            BasicDBList collectionData = (BasicDBList) data.get(collection);
            DBCollection c = db.getCollection(collection);
            for (Object dataObject : collectionData) {
                c.insert((DBObject) dataObject);
            }
        }
    }

    private DBObject readJson() throws IOException {
        Resource r = new ClassPathResource("fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-tulos-mockdata.json");
        StringWriter w = new StringWriter();
        IOUtils.copy(r.getInputStream(), w);
        return (DBObject) JSON.parse(w.toString());
    }
}
