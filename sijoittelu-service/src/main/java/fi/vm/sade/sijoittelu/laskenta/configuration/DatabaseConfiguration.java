package fi.vm.sade.sijoittelu.laskenta.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@EnableJdbcRepositories(
  basePackages = {
    "fi.vm.sade.valintalaskenta.tulos.dao.repository"
  })
public class DatabaseConfiguration extends AbstractJdbcConfiguration {

  @Bean
  public DataSource dataSourceValintalaskenta(
    @Value("${valintalaskenta-laskenta-service.postgresql.maxactive}") final String maxPoolSize,
    @Value("${valintalaskenta-laskenta-service.postgresql.maxwait}") final String maxWait,
    @Value("${valintalaskenta-laskenta-service.postgresql.leakdetectionthresholdmillis}")
    final String leaksThreshold,
    @Value("${valintalaskenta-laskenta-service.postgresql.url}") final String url,
    @Value("${valintalaskenta-laskenta-service.postgresql.user}") final String user,
    @Value("${valintalaskenta-laskenta-service.postgresql.password}") final String password,
    @Value("${valintalaskenta-laskenta-service.postgresql.driver}") final String driverClassName,
    @Value("${valintalaskenta-laskenta-service.postgresql.readonly:true}") final String readOnly
    ) {
    final HikariConfig config = new HikariConfig();
    config.setPoolName("springHikariCP");
    config.setConnectionTestQuery("SELECT 1");
    config.setJdbcUrl(url);
    config.setDriverClassName(driverClassName);
    config.setMaximumPoolSize(Integer.parseInt(maxPoolSize));
    config.setMaxLifetime(Long.parseLong(maxWait));
    config.setLeakDetectionThreshold(Long.parseLong(leaksThreshold));
    config.setRegisterMbeans(false);
    config.setReadOnly(Boolean.parseBoolean(readOnly));
    final Properties dsProperties = new Properties();
    dsProperties.setProperty("url", url);
    dsProperties.setProperty("user", user);
    dsProperties.setProperty("password", password);
    config.setDataSourceProperties(dsProperties);
    return new HikariDataSource(config);
  }

  @Bean
  NamedParameterJdbcOperations namedParameterJdbcOperations(DataSource dataSourceValintalaskenta) {
    return new NamedParameterJdbcTemplate(dataSourceValintalaskenta);
  }

  @Bean
  TransactionManager transactionManager(DataSource dataSourceValintalaskenta) {
    return new DataSourceTransactionManager(dataSourceValintalaskenta);
  }

}
