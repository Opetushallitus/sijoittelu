package fi.vm.sade.sijoittelu.tulos.resource;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public abstract class SijoitteluResourceTest {

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    ObjectMapper objectMapper = new ObjectMapper();

    String hakuOid = "1.2.246.562.5.2013080813081926341928";
    String sijoitteluAjoId = "latest";
    String hakemusOid = "1.2.246.562.11.00000441369";
}
