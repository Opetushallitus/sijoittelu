package fi.vm.sade.sijoittelu.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VastaanotettavuusDTO {
    private List<VastaanottoAction> allowedActions;

    private String reason;

    public List<VastaanottoAction> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(List<VastaanottoAction> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isVastaanotettavissa() {
        return allowedActions != null && allowedActions.stream().anyMatch(VastaanottoAction::isVastaanotettavissa);
    }

    public static enum VastaanottoActionValue {
        VastaanotaSitovasti, VastaanotaEhdollisesti, Peru;
    }

    public static class VastaanottoAction {
        private static final Map<ValintatuloksenTila, VastaanottoActionValue> tilaToAction;

        static {
            tilaToAction = new HashMap<>();
            tilaToAction.put(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, VastaanottoActionValue.VastaanotaSitovasti);
            tilaToAction.put(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, VastaanottoActionValue.VastaanotaEhdollisesti);
            tilaToAction.put(ValintatuloksenTila.PERUNUT, VastaanottoActionValue.Peru);
        }

        private VastaanottoActionValue action;

        public VastaanottoAction() {
        }

        public VastaanottoAction(VastaanottoActionValue vastaanottoActionValue) {
            if (vastaanottoActionValue == null) {
                throw new IllegalArgumentException("vastaanottoActionValue is null");
            }
            action = vastaanottoActionValue;
        }

        public void setAction(VastaanottoActionValue action) {
            this.action = action;
        }

        public VastaanottoActionValue getAction() {
            return action;
        }

        public boolean isVastaanotettavissa() {
            return action == VastaanottoActionValue.VastaanotaSitovasti || action == VastaanottoActionValue.VastaanotaEhdollisesti;
        }

        public static VastaanottoAction of(ValintatuloksenTila tila) {
            return new VastaanottoAction(tilaToAction.get(tila));
        }
    }
}
