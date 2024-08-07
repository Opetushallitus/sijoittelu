package fi.vm.sade.sijoittelu.jatkuva.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import fi.vm.sade.sijoittelu.jatkuva.dto.Sijoittelu;
import fi.vm.sade.sijoittelu.laskenta.service.business.ToteutaSijoitteluService;
import fi.vm.sade.sijoittelu.jatkuva.service.SijoittelunAktivointiService;
import fi.vm.sade.sijoittelu.jatkuva.service.SijoittelunValvontaService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SijoitteluRouteImpl implements SijoittelunValvontaService, SijoittelunAktivointiService {

  private static final Logger LOG = LoggerFactory.getLogger(SijoitteluRouteImpl.class);
  private final ToteutaSijoitteluService toteutaSijoitteluService;
  private final Cache<String, Sijoittelu> sijoitteluCache =
      CacheBuilder.newBuilder()
          .weakValues()
          .expireAfterWrite(3, TimeUnit.HOURS)
          .removalListener(
              (RemovalListener<String, Sijoittelu>)
                  notification -> LOG.info("{} siivottu pois muistista", notification.getValue()))
          .build();

  @Autowired
  public SijoitteluRouteImpl(ToteutaSijoitteluService toteutaSijoitteluService) {
    this.toteutaSijoitteluService = toteutaSijoitteluService;
  }

  @Override
  public Sijoittelu haeAktiivinenSijoitteluHaulle(String hakuOid) {
    return sijoitteluCache.getIfPresent(hakuOid);
  }

  @Override
  public void aktivoiSijoittelu(Sijoittelu sijoittelu) {
    try {
      Sijoittelu vanhaSijoittelu = sijoitteluCache.getIfPresent(sijoittelu.getHakuOid());
      if (vanhaSijoittelu != null && vanhaSijoittelu.isTekeillaan()) {
        // varmistetaan etta uudelleen ajon reunatapauksessa
        // mahdollisesti viela suorituksessa oleva vanha
        // laskenta
        // lakkaa kayttamasta resursseja ja siivoutuu ajallaan
        // pois
        throw new RuntimeException(
            "Sijoittelu haulle " + sijoittelu.getHakuOid() + " on jo kaynnissa!");
      } else {
        sijoitteluCache.put(sijoittelu.getHakuOid(), sijoittelu);
      }

      LOG.info("Aloitetaan sijoittelu haulle {}", sijoittelu.getHakuOid());
      toteutaSijoitteluService.toteutaSijoitteluAsync(
          sijoittelu.getHakuOid(),
          success -> {
            sijoittelu.setValmis();
          },
          e -> {
            LOG.error("Sijoittelu epaonnistui haulle " + sijoittelu.getHakuOid(), e);
            sijoittelu.setOhitettu();
          });
    } catch (Exception e) {
      if (sijoittelu.isTekeillaan()) {
        sijoittelu.setOhitettu();
      }
      LOG.error("Sijoittelu paattyi virheeseen {}\r\n{}", e.getMessage(), e.getStackTrace());
    }
  }
}
