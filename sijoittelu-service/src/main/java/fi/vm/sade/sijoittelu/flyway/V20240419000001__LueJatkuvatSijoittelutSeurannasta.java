package fi.vm.sade.sijoittelu.flyway;

import com.google.gson.reflect.TypeToken;
import fi.vm.sade.sijoittelu.jatkuva.dto.JatkuvaSijoittelu;
import fi.vm.sade.sijoittelu.jatkuva.external.resource.viestintapalvelu.RestCasClient;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

public class V20240419000001__LueJatkuvatSijoittelutSeurannasta extends BaseJavaMigration {

  private static final Logger LOG = LoggerFactory.getLogger(V20240419000001__LueJatkuvatSijoittelutSeurannasta.class);

  private static RestCasClient seurantaCasClient;
  private static UrlProperties urlProperties;

  private static boolean isTest;

  // ainakin hahtuvalla seurantapalvelu antaa ulos osittain täyttä roskaa, joten suodatetaan sijoittelut
  // joilla validi oid
  private static final Pattern OID_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]+)*$");

  public static void setDependencies(UrlProperties properties, RestCasClient client, boolean test) {
    urlProperties = properties;
    seurantaCasClient = client;
    isTest = test;
  }

  public Collection<JatkuvaSijoittelu> hae() {
    try {
      return seurantaCasClient
          .get(
              urlProperties.url("valintalaskentakoostepalvelu.seuranta.rest.url")
                  + "/sijoittelunseuranta/hae",
              new TypeToken<Collection<JatkuvaSijoittelu>>() {},
              Collections.emptyMap(),
              10 * 60 * 1000)
          .get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void migrate(Context context) {
    if(isTest) {
      LOG.warn("Flyway migration skipped because we are running tests");
    } else {
      JdbcTemplate template = new JdbcTemplate();
      template.setDataSource(new SingleConnectionDataSource(context.getConnection(), true));
      for(JatkuvaSijoittelu jatkuvaSijoittelu : this.hae()) {
        if(OID_PATTERN.matcher(jatkuvaSijoittelu.getHakuOid()).matches()) {
          template.update(
              "INSERT INTO jatkuvat " +
                  "(haku_oid, jatkuva_paalla, viimeksi_ajettu, aloitus, ajotiheys) " +
                  "VALUES (?, ?, ?::timestamptz, ?::timestamptz, ?)",
              jatkuvaSijoittelu.getHakuOid(),
              jatkuvaSijoittelu.isAjossa(),
              jatkuvaSijoittelu.getViimeksiAjettu(),
              jatkuvaSijoittelu.getAloitusajankohta(),
              jatkuvaSijoittelu.getAjotiheys());
        }
      }
    }
  }
}
