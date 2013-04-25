package fi.vm.sade.sijoittelu.ui.event;

import com.vaadin.event.ComponentEventListener;

@SuppressWarnings("serial")
public abstract class SijoitteluajoRefreshEventListener implements ComponentEventListener {
    public abstract void sijoitteluajoRefresh(Long sijoitteluajoId);

    public abstract void sijoitteluajoRefresh();
}
