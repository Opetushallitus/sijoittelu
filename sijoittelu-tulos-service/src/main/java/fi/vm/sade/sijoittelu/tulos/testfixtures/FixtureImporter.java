package fi.vm.sade.sijoittelu.tulos.testfixtures;

import com.mongodb.DB;
import com.mongodb.DBObject;

public class FixtureImporter {
    public static void importFixtures(final DB db, final String fixtureName) {
        DBObject base = MongoMockData.readJson("fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json");
        MongoMockData.insertData(db, base);
        DBObject tulokset = MongoMockData.readJson("fi/vm/sade/sijoittelu/tulos/resource/"+ fixtureName);
        MongoMockData.insertData(db, tulokset);
    }
}
