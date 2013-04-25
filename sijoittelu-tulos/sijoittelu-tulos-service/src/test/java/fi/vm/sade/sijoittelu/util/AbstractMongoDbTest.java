package fi.vm.sade.sijoittelu.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * Abstract base class for test cases that need to use mongo db. The class
 * starts mongo db before tests are run and stops it when the test run is
 * finished
 * 
 * @author wuoti
 * 
 */
public abstract class AbstractMongoDbTest {

    protected static final int PORT = 37017;

    protected static MongodExecutable mongodExe;
    protected static MongodProcess mongod;

    protected static Mongo mongo;

    @BeforeClass
    public static void setUp() throws Exception {
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        mongodExe = runtime.prepare(new MongodConfig(Version.Main.V2_0, PORT, Network.localhostIsIPv6()));
        mongod = mongodExe.start();

        mongo = new Mongo("localhost", PORT);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        mongod.stop();
        mongodExe.stop();
    }

    protected Mongo getMongo() {
        return mongo;
    }
}
