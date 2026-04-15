package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.valintatulosservice.VastaanottoService;
import fi.vm.sade.valintatulosservice.valintarekisteri.domain.VastaanottoEventDto;
import scala.jdk.javaapi.CollectionConverters;

import java.util.List;

public class WrappedVastaanottoService {
  // Laitettu VTS:n VastaanottoService wrapperiin, jotta ei tule houkutuksia käyttää muita sen toiminnallisuuksia ilman tarkempaa analyysiä
  private final VastaanottoService vastaanottoService;

  public WrappedVastaanottoService(VastaanottoService vastaanottoService) {
    this.vastaanottoService = vastaanottoService;
  }

  public void vastaanotaVirkailijanaInTransaction(List<VastaanottoEventDto> vs) {
    vastaanottoService.vastaanotaVirkailijanaInTransaction(CollectionConverters.asScala(vs.iterator()).toList());
  }
}
