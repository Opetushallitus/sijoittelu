package fi.vm.sade.sijoittelu.jatkuva.external.resource.tarjonta;

import java.util.concurrent.CompletableFuture;

public interface TarjontaAsyncResource {
  CompletableFuture<Haku> haeHaku(String hakuOid);
}
