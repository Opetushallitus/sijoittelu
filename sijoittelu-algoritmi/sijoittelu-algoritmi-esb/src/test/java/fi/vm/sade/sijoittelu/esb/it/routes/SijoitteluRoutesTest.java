package fi.vm.sade.sijoittelu.esb.it.routes;

import fi.vm.sade.service.sijoittelu.SijoitteluService;
import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.sijoittelu.batch.dao.Dao;

import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author Eetu Blomqvist
 */
public class SijoitteluRoutesTest extends CamelSpringTestSupport {

    SijoitteluService sijoitteluService;

    @Override
    protected AbstractApplicationContext createApplicationContext() {
//        return new ClassPathXmlApplicationContext("classpath:META-INF/spring/it-test-context.xml");
        return new ClassPathXmlApplicationContext("it-test-context.xml");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sijoitteluService = applicationContext.getBean("sijoitteluService", SijoitteluService.class);
    }

    @Test
    public void testSijoittelu() {

        SijoitteleTyyppi sijoittele = null;
        try {
            JAXBContext jc = JAXBContext.newInstance("fi.vm.sade.service.sijoittelu.types");
            InputStream stream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("data/sijoitteludata_2011K_ALPAT.xml");
            JAXBElement<SijoitteleTyyppi> je = (JAXBElement<SijoitteleTyyppi>) jc.createUnmarshaller()
                    .unmarshal(stream);
            sijoittele = je.getValue();

        } catch (JAXBException e) {
            e.printStackTrace();
            fail();
        }

        NotifyBuilder notify = new NotifyBuilder(context).whenCompleted(1).create();
        sijoitteluService.sijoittele(sijoittele);
        assertTrue(notify.matches(10, TimeUnit.SECONDS));
    }

}
