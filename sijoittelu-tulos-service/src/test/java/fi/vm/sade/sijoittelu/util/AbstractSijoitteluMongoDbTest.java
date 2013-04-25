package fi.vm.sade.sijoittelu.util;

import com.google.code.morphia.Datastore;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSijoitteluMongoDbTest extends AbstractMongoDbTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSijoitteluMongoDbTest.class);

    @Autowired
    private Datastore morphiaDS;

    @After
    public void after() {
        logger.info("Dropping " + morphiaDS.getDB().getName() + " database");
        mongo.dropDatabase(morphiaDS.getDB().getName());
    }
}
