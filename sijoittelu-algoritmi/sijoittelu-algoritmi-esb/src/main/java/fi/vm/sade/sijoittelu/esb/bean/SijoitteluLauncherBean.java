package fi.vm.sade.sijoittelu.esb.bean;

import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.SijoitteluBusinessService;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.component.cxf.CxfPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;

/**
 * @author Eetu Blomqvist
 */
@Component("sijoitteluLauncher")
public class SijoitteluLauncherBean {

    private static Logger logger = LoggerFactory.getLogger(SijoitteluLauncherBean.class);

    @Autowired
    private SijoitteluBusinessService sijoitteluService;

    public void launch(@Body CxfPayload cxfPayload, Exchange exchange) {

        Source source = (Source) cxfPayload.getBodySources().get(0);

        // Parse body to bean
        SijoitteleTyyppi sijoittele = null;
        try {
            JAXBContext jc = JAXBContext.newInstance(SijoitteleTyyppi.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            sijoittele = unmarshaller.unmarshal(source, SijoitteleTyyppi.class).getValue();

            exchange.getOut().setHeader("SijoitteluId", sijoittele.getSijoitteluId());

        } catch (JAXBException e) {
            logger.error("xml unmarshalling failed", e);
            exchange.setException(e);
            exchange.getOut().setFault(true);
            throw new RuntimeException("Failed");
        }

        // execute algorithm
        logger.info("Executing sijoittelu");
        sijoitteluService.sijoittele(sijoittele);
        logger.info("done!");
    }


}
