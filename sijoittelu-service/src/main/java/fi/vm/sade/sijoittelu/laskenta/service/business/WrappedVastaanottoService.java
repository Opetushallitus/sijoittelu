package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.valintatulosservice.VastaanottoService;
import fi.vm.sade.valintatulosservice.valintarekisteri.domain.VastaanottoEventDto;
import scala.jdk.javaapi.CollectionConverters;
import scala.runtime.BoxedUnit;
import scala.util.Try;

import java.util.List;

public class WrappedVastaanottoService {
  // Laitettu VTS:n VastaanottoService wrapperiin, jotta ei tule houkutuksia käyttää muita sen toiminnallisuuksia ilman tarkempaa analyysiä
  private final VastaanottoService vastaanottoService;

  public WrappedVastaanottoService(VastaanottoService vastaanottoService) {
    this.vastaanottoService = vastaanottoService;
  }

  public void vastaanotaVirkailijanaInTransaction(List<VastaanottoEventDto> vs) {
    // VTS palauttaa Try[Unit]:n eikä heitä poikkeusta epäonnistuessaan, joten Failure on tarkistettava
    // eksplisiittisesti. Muuten tallennuksen epäonnistuminen jäisi täysin huomaamatta
    // ja sijoittelu jatkaisi tulostensa tallentamista ristiriitaiseen tilaan.
    Try<BoxedUnit> result = vastaanottoService.vastaanotaVirkailijanaInTransaction(
        CollectionConverters.asScala(vs.iterator()).toList());
    if (result.isFailure()) {
      throw new RuntimeException("Vastaanottojen tallennus valintarekisteriin epäonnistui", result.failed().get());
    }
  }
}
