package fi.vm.sade.sijoittelu.laskenta.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HakukohteenValintatulosUpdateStatuses {
    public final String message;
    public final List<ValintatulosUpdateStatus> statuses;

    public HakukohteenValintatulosUpdateStatuses(List<ValintatulosUpdateStatus> statuses) {
        this.message = null;
        this.statuses = statuses;
    }

    public HakukohteenValintatulosUpdateStatuses(String message, List<ValintatulosUpdateStatus> statuses) {
        this.message = message;
        this.statuses = statuses;
    }
}
