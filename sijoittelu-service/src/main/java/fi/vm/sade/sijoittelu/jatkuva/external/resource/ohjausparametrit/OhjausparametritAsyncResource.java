package fi.vm.sade.sijoittelu.jatkuva.external.resource.ohjausparametrit;

import fi.vm.sade.sijoittelu.jatkuva.external.resource.ohjausparametrit.dto.ParametritDTO;

import java.util.concurrent.CompletableFuture;

public interface OhjausparametritAsyncResource {
  CompletableFuture<ParametritDTO> haeHaunOhjausparametrit(String hakuOid);
}
