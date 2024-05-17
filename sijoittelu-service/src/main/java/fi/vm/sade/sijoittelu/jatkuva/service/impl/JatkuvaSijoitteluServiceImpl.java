package fi.vm.sade.sijoittelu.jatkuva.service.impl;

import fi.vm.sade.sijoittelu.jatkuva.dao.JatkuvaSijoitteluDAO;
import fi.vm.sade.sijoittelu.jatkuva.dto.JatkuvaSijoittelu;
import fi.vm.sade.sijoittelu.jatkuva.dto.AjastettuSijoitteluInfo;
import fi.vm.sade.sijoittelu.jatkuva.service.JatkuvaSijoitteluService;
import fi.vm.sade.sijoittelu.jatkuva.util.Formatter;
import fi.vm.sade.sijoittelu.jatkuva.job.AjastettuSijoitteluJob;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ws.rs.NotAuthorizedException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

public class JatkuvaSijoitteluServiceImpl implements JatkuvaSijoitteluService {
  private static final Logger LOG = LoggerFactory.getLogger(JatkuvaSijoitteluServiceImpl.class);

  private final JatkuvaSijoitteluDAO sijoittelunSeurantaResource;
  private final int VAKIO_AJOTIHEYS = 24;
  private final Timer timer;
  private final Scheduler sijoitteluScheduler;

  private Map<String, AjastettuSijoitteluInfo> ajastetutSijoitteluInfot = new HashMap<>();

  private Timer createAjastettujenSijoitteluidenPaivittaja(boolean start, long runEveryMinute) {
    Timer timer = new Timer("JatkuvaSijoitteluTimer");
    if (start) {
      TimerTask repeatedTask =
          new TimerTask() {
            public void run() {
              try {
                JatkuvaSijoitteluServiceImpl.this.teeJatkuvaSijoittelu();
              } catch (Exception e) {
                LOG.error("Exception in JatkuvaSijoitteluTimerTask", e);
              }
            }
          };
      long delay = 1000L;
      long period = TimeUnit.MINUTES.toMillis(runEveryMinute);
      timer.scheduleAtFixedRate(repeatedTask, delay, period);
    }
    return timer;
  }

  @Autowired
  public JatkuvaSijoitteluServiceImpl(
      @Value("${jatkuvasijoittelu.autostart:true}") boolean autoStartup,
      @Value("${valintalaskentakoostepalvelu.jatkuvasijoittelu.intervalMinutes:5}")
          long jatkuvaSijoitteluPollIntervalInMinutes,
      JatkuvaSijoitteluDAO sijoittelunSeurantaResource,
      SchedulerFactoryBean schedulerFactoryBean) {
    this.sijoittelunSeurantaResource = sijoittelunSeurantaResource;
    this.timer =
        createAjastettujenSijoitteluidenPaivittaja(
            autoStartup, jatkuvaSijoitteluPollIntervalInMinutes);
    this.sijoitteluScheduler = schedulerFactoryBean.getScheduler();
    try {
      sijoitteluScheduler.start();
    } catch (SchedulerException se) {
      throw new RuntimeException("Sijoitteluiden ajastimen luominen epäonnistui", se);
    }
  }

  @Override
  public void teeJatkuvaSijoittelu() {
    LOG.info("Sijoitteluiden ajastin kaynnistyi");
    Map<String, JatkuvaSijoittelu> aktiivisetSijoittelut = getAktiivisetSijoittelut();
    LOG.info(
        "Sijoitteluiden ajastin sai seurannalta {} aktiivista sijoittelua.",
        aktiivisetSijoittelut.size());
    ajastetutSijoitteluInfot =
        aktiivisetSijoittelut.values().stream()
            .map(
                dto ->
                    new AjastettuSijoitteluInfo(
                        dto.getHakuOid(), dto.getAloitusajankohta(), dto.getAjotiheys()))
            .collect(Collectors.toMap(AjastettuSijoitteluInfo::getHakuOid, Function.identity()));
    poistaSammutetut(aktiivisetSijoittelut);
    laitaAjoon(aktiivisetSijoittelut);
  }

  @Override
  public List<AjastettuSijoitteluInfo> haeAjossaOlevatAjastetutSijoittelut() {
    try {
      return sijoitteluScheduler.getJobGroupNames().stream()
          .map(hakuOid -> ajastetutSijoitteluInfot.get(hakuOid))
          .collect(Collectors.toList());
    } catch (SchedulerException se) {
      throw new RuntimeException(se);
    }
  }

  private void laitaAjoon(Map<String, JatkuvaSijoittelu> aktiivisetSijoittelut) {
    aktiivisetSijoittelut.forEach(
        (hakuOid, sijoitteluDto) -> {
          if (sijoitteluDto.getAloitusajankohta() == null || sijoitteluDto.getAjotiheys() == null) {
            LOG.warn(
                "Ajastettua sijoittelua ei suoriteta haulle {} koska siltä puuttuu pakollisia parametreja.",
                hakuOid);
          } else if (sijoitteluDto.getAjotiheys() <= 0) {
            LOG.warn(
                "Ajastettua sijoittelua ei suoriteta haulle {} koska ajotieheys {} ei ole positiivinen.",
                hakuOid,
                sijoitteluDto.getAjotiheys());
          } else {
            try {
              // Käytetään minuutin tarkkuutta aloitusajankohdassa jotta ensimmäinen cron ei aikaisemmin
              // kuin startAt
              Date asetusAjankohta = getAloitusajankohta(sijoitteluDto).withSecondOfMinute(0).toDate();
              Integer intervalli = getAjotiheys(sijoitteluDto);

              String jobName =
                  String.valueOf(asetusAjankohta.hashCode())
                      .concat("#")
                      .concat(String.valueOf(intervalli.hashCode()));

              if (!sijoitteluScheduler.checkExists(JobKey.jobKey(jobName, hakuOid))) {
                sijoitteluScheduler.deleteJobs(
                    new ArrayList<>(
                        sijoitteluScheduler.getJobKeys(GroupMatcher.groupEquals(hakuOid))));
                JobDetail sijoitteluJob =
                    JobBuilder.newJob(AjastettuSijoitteluJob.class)
                        .withIdentity(jobName, hakuOid)
                        .usingJobData("hakuOid", hakuOid)
                        .build();

                String timezoneId = "Europe/Helsinki";
                DateTime aloitusDateTime =
                    new DateTime(asetusAjankohta, DateTimeZone.forID(timezoneId));
                int aloitusTunnit = aloitusDateTime.getHourOfDay();
                int aloitusMinuutit = aloitusDateTime.getMinuteOfHour();
                String cron =
                    String.format(
                        "0 %s %s ? * * *",
                        aloitusMinuutit,
                        intervalli > 23
                            ? aloitusTunnit
                            : String.format("%s/%s", aloitusTunnit, intervalli));
                CronScheduleBuilder cronScheduleBuilder =
                    CronScheduleBuilder.cronSchedule(cron)
                        .inTimeZone(TimeZone.getTimeZone(timezoneId))
                        .withMisfireHandlingInstructionDoNothing();

                Trigger sijoitteluCronTrigger =
                    TriggerBuilder.newTrigger()
                        .withIdentity(jobName, jobName)
                        .startAt(asetusAjankohta)
                        .withSchedule(cronScheduleBuilder)
                        .build();

                LOG.info(
                    "Ajastettu sijoittelu haulle {} joka on asetettu {} intervallilla {} ajastetaan (datetime: {} | tunnit: {} | minuutit: {}). CRON: {}",
                    hakuOid,
                    Formatter.paivamaara(asetusAjankohta),
                    intervalli,
                    aloitusDateTime,
                    aloitusTunnit,
                    aloitusMinuutit,
                    cron);

                Set<Trigger> triggers = new HashSet<>();
                triggers.add(sijoitteluCronTrigger);

                sijoitteluScheduler.scheduleJob(sijoitteluJob, triggers, true);
              }
            } catch (SchedulerException se) {
              LOG.error("Ajastetun sijoittelun lisääminen haulle {} epäonnistui", hakuOid, se);
            }
          }
        });
  }

  private void poistaSammutetut(Map<String, JatkuvaSijoittelu> aktiivisetSijoittelut) {
    try {
      sijoitteluScheduler
          .getJobGroupNames()
          .forEach(
              hakuOid -> {
                try {
                  if (!aktiivisetSijoittelut.containsKey(hakuOid)) {
                    List<JobKey> jobKeys =
                        new ArrayList<>(
                            sijoitteluScheduler.getJobKeys(GroupMatcher.groupEquals(hakuOid)));
                    sijoitteluScheduler.deleteJobs(jobKeys);
                    LOG.warn("Sijoittelu haulle {} poistettu ajastuksesta.", hakuOid);
                  }
                } catch (SchedulerException se) {
                  LOG.error("Ajastetun sijoittelun siivoaminen haulle {} epäonnistui", hakuOid, se);
                }
              });

    } catch (Exception e) {
      LOG.error("Ajastettujen sijoittelujen siivous epäonnistui", e);
    }
  }

  private Map<String, JatkuvaSijoittelu> getAktiivisetSijoittelut() {
    try {
      return _getAktiivisetSijoittelut();
    } catch (NotAuthorizedException e) {
      LOG.warn("Aktiivisten sijoittelujen haku epäonnistui. Yritetään uudelleen.", e);
      return _getAktiivisetSijoittelut(); // FIXME kill me OK-152!
    }
  }

  private Map<String, JatkuvaSijoittelu> _getAktiivisetSijoittelut() {
    return sijoittelunSeurantaResource.hae().stream()
        .filter(Objects::nonNull)
        .filter(JatkuvaSijoittelu::isAjossa)
        .filter(
            sijoitteluDto -> {
              DateTime aloitusajankohta = getAloitusajankohta(sijoitteluDto);
              return aktivoidaanko(aloitusajankohta);
            })
        .collect(Collectors.toMap(JatkuvaSijoittelu::getHakuOid, s -> s));
  }

  public int getAjotiheys(JatkuvaSijoittelu jatkuvaSijoittelu) {
    return Optional.ofNullable(jatkuvaSijoittelu.getAjotiheys()).orElse(VAKIO_AJOTIHEYS);
  }

  public boolean aktivoidaanko(DateTime aloitusAika) {
    return aloitusAika.isBefore(DateTime.now().plusHours(1));
  }

  private DateTime getAloitusajankohta(JatkuvaSijoittelu jatkuvaSijoittelu) {
    return new DateTime(
        Optional.ofNullable(jatkuvaSijoittelu.getAloitusajankohta()).orElse(new Date()));
  }
}
