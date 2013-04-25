package fi.vm.sade.sijoittelu.ui.event;

import com.vaadin.event.ComponentEventListener;

@SuppressWarnings("serial")
public abstract class HakuListingRefreshEventListener implements ComponentEventListener {
    public abstract void refreshHakuListing();
}
