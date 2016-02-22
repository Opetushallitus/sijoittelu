package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

import java.util.List;

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
        private VastaanottoActionValue action;

        public void setAction(VastaanottoActionValue action) {
            this.action = action;
        }

        public VastaanottoActionValue getAction() {
            return action;
        }

        public boolean isVastaanotettavissa() {
            return action == VastaanottoActionValue.VastaanotaSitovasti || action == VastaanottoActionValue.VastaanotaEhdollisesti;
        }
    }
}
