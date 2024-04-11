package fi.vm.sade.valinta.kooste.external.resource.tarjonta;

import java.util.concurrent.CompletableFuture;

public interface TarjontaAsyncResource {
  CompletableFuture<Haku> haeHaku(String hakuOid);
}
