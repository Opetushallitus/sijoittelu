package fi.vm.sade.sijoittelu.jatkuva.external.resource.organisaatio;

import java.util.concurrent.CompletableFuture;

public interface OrganisaatioAsyncResource {
  CompletableFuture<String> parentoids(String organisaatioId) throws Exception;
}
