package fi.vm.sade.configuration;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
public class MongoConfiguration {

    public static final String DATABASE_TEST_NAME = "test";

    @Bean
    public Mongod mongoExecutable() {
        return Mongod.builder().build();
    }

    @Bean(destroyMethod = "close")
    public TransitionWalker.ReachedState<RunningMongodProcess> mongodProcess() {
        return Mongod.instance().start(Version.Main.V3_6);
    }

    @Bean
    public MongoClient mongoClient(
        TransitionWalker.ReachedState<RunningMongodProcess> mongodProcess) {
        return new MongoClient("127.0.0.1", mongodProcess.current().getServerAddress().getPort());
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
