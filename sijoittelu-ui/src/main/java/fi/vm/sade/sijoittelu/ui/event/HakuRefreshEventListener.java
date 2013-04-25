package fi.vm.sade.sijoittelu.ui.event;

import com.vaadin.event.ComponentEventListener;

@SuppressWarnings("serial")
public abstract class HakuRefreshEventListener implements ComponentEventListener {
    public abstract void hakuRefresh(String hakuOid);
    public abstract void hakuRefresh();
}
