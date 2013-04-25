//package fi.vm.sade.sijoittelu.batch;
//
//import java.net.UnknownHostException;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//
//import com.google.code.morphia.Datastore;
//import com.google.code.morphia.Morphia;
//import com.mongodb.DB;
//import com.mongodb.Mongo;
//
///**
// * 
// * @author Kari Kammonen
// * 
// */
//@Configuration
//@ComponentScan(basePackages = "fi.vm.sade.sijoittelu.batch")
//// @EnableTransactionManagement(proxyTargetClass = true)
//// @PropertySource("classpath:persistence.properties")
//// @Import(SpringMvcConfiguration.class)
//// @Profile("default")
//public class ApplicationConfiguration {
//
//    // @Value("${mongodb.dbname}")
//    private String dbname = "sijoittelu";
//
//    // @Value("${mongodb.url.sijoittelu}")
//    private String dbHost = "localhost";
//
//    // @Value("${mongodb.port}")
//    private int dbPort = 27017;
//
//    @Bean
//    public Mongo mongo() throws UnknownHostException {
//        return new Mongo();
//    }
//
//    @Bean
//    public DB db() throws UnknownHostException {
//        return mongo().getDB(dbname);
//    }
//
//    @Bean
//    public Morphia morphia() {
//        return new Morphia();
//    }
//
//    @Bean
//    public Datastore morphiaDS() throws UnknownHostException {
//        return morphia().createDatastore(mongo(), dbname);
//    }
//
// }