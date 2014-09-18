package fi.vm.sade.sijoittelu.tulos.service;

import com.mongodb.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

import fi.vm.sade.sijoittelu.tulos.testfixtures.FixtureImporter;

@Service
@Profile("it")
public class DataImportService {
    @Autowired
    MongoClient mongo;

    @Value("${sijoittelu-service.mongodb.dbname}")
    String dbname;

    @PostConstruct
    public void importData() throws IOException {
        FixtureImporter.importFixture(mongo.getDB(dbname), "hyvaksytty-ilmoitettu.json");
    }
}
