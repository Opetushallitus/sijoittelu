package fi.vm.sade.mongo;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.NullProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.runtime.Network;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class MongoConfiguration {
    private static final Logger LOG = LoggerFactory
            .getLogger(MongoConfiguration.class);
    public static final String DATABASE_TEST_NAME = "test";

    private static int freePort() {
        for (int i = 0; i < 10; ++i) {
            try {
                return Network.getFreeServerPort();
            } catch (IOException e) {
            }
        }
        return 32452 - new Random(System.currentTimeMillis()).nextInt(20000);
    }

    // fake mongo db
    @Bean(destroyMethod = "stop")
    public MongodExecutable getMongodExecutable() throws IOException {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        java.util.logging.Logger.getLogger("global").setLevel(Level.OFF);
        final int PORT = freePort();
        LOG.error("### Mongo kaynnistyy porttiin {}", PORT);

        IStreamProcessor mongodOutput = Processors.named("[mongod>]",
                new NullProcessor());

        IStreamProcessor mongodError = new NullProcessor();//new FileStreamProcessor(File.createTempFile("mongod-error", "log"));
        IStreamProcessor commandsOutput = new NullProcessor();//Processors.namedConsole("[console>]");

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(Command.MongoD)
                .processOutput(new ProcessOutput(mongodOutput, mongodError, commandsOutput))
                .build();

        IMongodConfig mongodConfig = new MongodConfigBuilder()

                        .version(Version.Main.PRODUCTION)
                .net(new Net(Network.getLocalHost().getHostAddress(), PORT, Network.localhostIsIPv6()))

                .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

        MongodExecutable mongodExecutable = null;
        mongodExecutable = runtime.prepare(mongodConfig);


        return mongodExecutable;// .newMongo();
    }

    @Bean(destroyMethod = "stop")
    public MongodProcess getMongoProcess(MongodExecutable mongodExecutable)
            throws IOException {
        return mongodExecutable.start();
    }

    @Bean
    public MongoClient getMongo(MongodProcess process) throws IOException {
        return new MongoClient(new ServerAddress(Network.getLocalHost(), process.getConfig().net().getPort()));
    }

    @Bean(name="morphia2")
    public Morphia getMorphia2() {
        return new Morphia();
    }

    @Bean(name="datastore2")
    public Datastore getDatastore2(@Qualifier("morphia2") Morphia morphia, MongoClient mongo) {
        return morphia.createDatastore(mongo, DATABASE_TEST_NAME);
    }
}
