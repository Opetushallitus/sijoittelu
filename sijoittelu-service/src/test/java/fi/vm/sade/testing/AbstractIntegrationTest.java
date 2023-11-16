package fi.vm.sade.testing;

import fi.vm.sade.sijoittelu.App;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = {App.class},
  args = {"--add-opens=java.base/java.lang=ALL-UNNAMED"})
@Import(TestConfigurationWithMocks.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class AbstractIntegrationTest {

  private final static List<String> TABLES = List.of("jonosija", "muokattu_jonosija", "hakijaryhma", "valinnanvaihe", "valintatapajono", "jarjestyskriteeritulos");

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @LocalServerPort protected Integer port;

  @AfterEach
  public void cleanAfter() {
    List<String> operations = new ArrayList<>(TABLES.stream().map(table -> String.format("ALTER TABLE %s DISABLE TRIGGER ALL;", table)).toList());
    operations.addAll(TABLES.stream().map(table -> String.format("delete from %s;", table)).toList());
    operations.addAll(TABLES.stream().map(table -> String.format("ALTER TABLE %s DISABLE TRIGGER ALL;", table)).toList());
    jdbcTemplate.batchUpdate(operations.toArray(String[]::new));
  }

}
