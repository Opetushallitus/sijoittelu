package fi.vm.sade.sijoittelu.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.*;

@Configuration
public class SijoitteluDatabaseConfiguration {

  @Bean("sijoitteluDataSource")
  public DataSource sijoitteluDataSource(
    @Value("${sijoittelu.sijoittelu.postgresql.url}") final String url,
    @Value("${sijoittelu.sijoittelu.postgresql.username}") final String user,
    @Value("${sijoittelu.sijoittelu.postgresql.password}") final String password,
    @Value("${sijoittelu.sijoittelu.postgresql.driver}") final String driverClassName) {
    final HikariConfig config = new HikariConfig();
    config.setConnectionTestQuery("SELECT 1");
    config.setJdbcUrl(url);
    final Properties dsProperties = new Properties();
    dsProperties.setProperty("url", url);
    dsProperties.setProperty("user", user);
    dsProperties.setProperty("password", password);
    config.setDataSourceProperties(dsProperties);
    if(!driverClassName.equals("")) config.setDriverClassName(driverClassName);
    return new HikariDataSource(config);
  }

  @Bean("sijoitteluJdbcTemplate")
  JdbcTemplate sijoitteluJdbcTemplate(
      @Qualifier("sijoitteluDataSource") DataSource sijoitteluDataSource) {
    return new JdbcTemplate(sijoitteluDataSource);
  }
}
