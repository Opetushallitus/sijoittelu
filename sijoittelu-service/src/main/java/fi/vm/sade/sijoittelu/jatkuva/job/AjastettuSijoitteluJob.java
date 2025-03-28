package fi.vm.sade.sijoittelu.jatkuva.job;

import fi.vm.sade.sijoittelu.jatkuva.dao.JatkuvaSijoitteluDAO;
import fi.vm.sade.sijoittelu.laskenta.service.business.ToteutaSijoitteluService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class AjastettuSijoitteluJob extends QuartzJobBean {

  private static final Logger LOG = LoggerFactory.getLogger(AjastettuSijoitteluJob.class);

  public static String JOB_GROUP = "AJASTETTU_SIJOITTELU";

  @Autowired
  public AjastettuSijoitteluJob() {}

  @Override
  public void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {
      ApplicationContext applicationContext =
          (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
      ToteutaSijoitteluService toteutaSijoitteluService =
          applicationContext.getBean(ToteutaSijoitteluService.class);
      JatkuvaSijoitteluDAO sijoittelunSeurantaResource =
          applicationContext.getBean(JatkuvaSijoitteluDAO.class);

      String hakuOid = (String) context.getJobDetail().getJobDataMap().get("hakuOid");
      try {
        LOG.info("Ajastettu sijoittelu kaynnistyy nyt haulle {}", hakuOid);
        toteutaSijoitteluService.toteutaSijoitteluAsync(
            hakuOid,
            done -> {
              LOG.info("Jatkuva sijoittelu saatiin tehtya haulle {}", hakuOid);
              sijoittelunSeurantaResource.merkkaaSijoittelunAjetuksi(hakuOid);
              LOG.info("Jatkuva sijoittelu merkattiin ajetuksi haulle {}", hakuOid);
            },
            poikkeus -> {
              LOG.error(
                  "Jatkuvan sijoittelun suorittaminen ei onnistunut haulle " + hakuOid, poikkeus);
            });
      } catch (Exception e) {
        LOG.error(
            "Jatkuvasijoittelu paattyi virheeseen ({}) {}\r\n{}",
            hakuOid,
            e.getMessage(),
            e.getStackTrace());
      }
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }
}
