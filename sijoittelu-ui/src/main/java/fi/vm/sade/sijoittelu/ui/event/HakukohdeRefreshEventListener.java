package fi.vm.sade.sijoittelu.ui.event;

import com.vaadin.event.ComponentEventListener;

@SuppressWarnings("serial")
public abstract class HakukohdeRefreshEventListener implements ComponentEventListener {
    public abstract void hakukohdeRefresh(Long sijoitteluajoId, String hakukohdeOid);
    public abstract void hakukohdeRefresh();
}
