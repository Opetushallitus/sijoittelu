package fi.vm.sade.sijoittelu.configuration;

import fi.vm.sade.sijoittelu.jatkuva.dao.JatkuvaSijoitteluDAO;
import fi.vm.sade.sijoittelu.jatkuva.service.impl.JatkuvaSijoitteluServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Profile({"default", "dev"})
@Configuration
public class SijoitteluRouteConfig {

  @Bean
  public JatkuvaSijoitteluServiceImpl getJatkuvaSijoitteluRouteImpl(
      // riippuvuus Flyway-migraatioihin jotta tätä beania ei luoda ennen kuin jatkuvien sijoitteluiden migraatio
      // seurantapalvelusta ajettu. Tämän voin refaktoroida pois kun migraatio onnistuneesti suoritettu.
      FlywayConfiguration.FlywayMigrationDone flywayMigrationDone,
      @Value("${jatkuvasijoittelu.autostart:true}") boolean autoStartup,
      @Value("${valintalaskentakoostepalvelu.jatkuvasijoittelu.intervalMinutes:5}")
          long jatkuvaSijoitteluPollIntervalInMinutes,
      JatkuvaSijoitteluDAO jatkuvaSijoitteluDAO,
      SchedulerFactoryBean schedulerFactoryBean) {
    return new JatkuvaSijoitteluServiceImpl(
        autoStartup,
        jatkuvaSijoitteluPollIntervalInMinutes,
        jatkuvaSijoitteluDAO,
        schedulerFactoryBean);
  }

  @Bean
  public SchedulerFactoryBean schedulerFactoryBean() {
    SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
    scheduler.setApplicationContextSchedulerContextKey("applicationContext");
    return scheduler;
  }
}
