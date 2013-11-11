package fi.vm.sade.sijoittelu.batch.logic.impl.itutil;

import com.google.code.morphia.Datastore;
import com.mongodb.Mongo;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Kari Kammonen
 * 
 *         To disable logging:
 *         https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de/issues/9
 * 
 *         RuntimeConfig runtimeConfig = new RuntimeConfig();
 *         runtimeConfig.setMongodOutputConfig(new
 *         MongodProcessOutputConfig(Processors.logTo(logger, Level.INFO),
 *         Processors.logTo(logger, Level.SEVERE),
 *         Processors.named("[console>]",Processors.logTo(logger,
 *         Level.FINE)))); MongoDBRuntime runtime =
 *         MongoDBRuntime.getInstance(runtimeConfig);
 * 
 * 
 */
public abstract class FlapdoodleMongoDbTest {

    @Autowired
    private Datastore morphiaDS;
    private static final int PORT = 37123;
    private static MongodExecutable mongodExe;
    private static MongodProcess mongod;
    private static Mongo mongo;

    @BeforeClass
    public static void setUp() throws Exception {
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.setProcessOutput(new ProcessOutput(new DummyOutputProcessor(), new DummyOutputProcessor(),
                new DummyOutputProcessor()));
        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
        mongodExe = runtime.prepare(new MongodConfig(Version.Main.V2_0, PORT, false));
        mongod = mongodExe.start();
        mongo = new Mongo("localhost", PORT);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        mongod.stop();
        mongod.waitFor();
        mongodExe.stop();

    }

    @After
    public void after() {
        mongo.dropDatabase(morphiaDS.getDB().getName());
    }

}
