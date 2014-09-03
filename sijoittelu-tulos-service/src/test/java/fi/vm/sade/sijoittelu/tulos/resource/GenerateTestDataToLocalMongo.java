package fi.vm.sade.sijoittelu.tulos.resource;

import java.net.UnknownHostException;

import com.mongodb.Mongo;

import fi.vm.sade.sijoittelu.tulos.generator.TulosGenerator;

public class GenerateTestDataToLocalMongo {
    public final static void main(String... args) throws UnknownHostException {
        TulosGenerator.generateTestData(200, 50000, new Mongo("localhost").getDB("sijoittelu"));
    }
}
