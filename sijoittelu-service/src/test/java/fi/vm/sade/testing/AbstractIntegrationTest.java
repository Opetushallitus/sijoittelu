package fi.vm.sade.testing;

import fi.vm.sade.sijoittelu.App;
import fi.vm.sade.valintalaskenta.tulos.dao.repository.HarkinnanvarainenHyvaksyminenRepository;
import fi.vm.sade.valintalaskenta.tulos.dao.repository.MuokattuJonosijaRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = {App.class},
  args = {"--add-opens=java.base/java.lang=ALL-UNNAMED"})
@Import(TestConfigurationWithMocks.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

  @Autowired protected MuokattuJonosijaRepository muokattuJonosijaRepository;

  @Autowired
  protected HarkinnanvarainenHyvaksyminenRepository harkinnanvarainenHyvaksyminenRepository;

  @LocalServerPort protected Integer port;

  @BeforeAll
  static void init() {

  }

  @BeforeEach
  public void setUp() {
    harkinnanvarainenHyvaksyminenRepository.deleteAll();
    muokattuJonosijaRepository.deleteAll();
  }
}
