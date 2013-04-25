package fi.vm.sade.sijoittelu.ui;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.sijoittelu.ui.component.Breadcrumb;
import fi.vm.sade.sijoittelu.ui.event.HakuListingRefreshEventListener;
import fi.vm.sade.sijoittelu.ui.event.HakuRefreshEventListener;
import fi.vm.sade.sijoittelu.ui.event.HakukohdeRefreshEventListener;
import fi.vm.sade.sijoittelu.ui.event.SijoitteluajoRefreshEventListener;

@SuppressWarnings("serial")
public class TulosVerticalLayout extends VerticalLayout {

    private HakuListing hakuListing;
    private HakuDetails hakuDetails;
    private SijoitteluajoDetails sijoitteluajoDetails;
    private HakukohdeDetails hakukohdeDetails;

    private VerticalLayout mainLayout;
    private Breadcrumb breadcrumb;

    private List<SijoitteluajoRefreshEventListener> sijoitteluajoRefreshEventListeners = new ArrayList<SijoitteluajoRefreshEventListener>();
    private List<HakuRefreshEventListener> hakuRefreshEventListeners = new ArrayList<HakuRefreshEventListener>();
    private List<HakuListingRefreshEventListener> hakuListingRefreshEventListeners = new ArrayList<HakuListingRefreshEventListener>();
    private List<HakukohdeRefreshEventListener> hakukohdeRefreshEventListeners = new ArrayList<HakukohdeRefreshEventListener>();

    public TulosVerticalLayout() {
        mainLayout = new VerticalLayout();
        breadcrumb = new Breadcrumb();
        addComponent(breadcrumb);
        addComponent(mainLayout);
        listHakus();
    }

    public void listHakus() {
        mainLayout.removeAllComponents();
        breadcrumb.add(I18N.getMessage("tulosVerticalLayout.breadcrumb.hakuListing"), new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                for (HakuListingRefreshEventListener l : hakuListingRefreshEventListeners) {
                    l.refreshHakuListing();
                }
            }
        });

        if (hakuListing == null) {
            hakuListing = new HakuListing(hakuRefreshEventListeners);
        } else {
            hakuListing.refresh();
        }

        mainLayout.addComponent(hakuListing);
    }

    public void showHaku(String hakuOid) {
        if (hakuDetails == null) {
            hakuDetails = new HakuDetails(hakuOid, sijoitteluajoRefreshEventListeners);
        } else {
            hakuDetails.setHakuOid(hakuOid);
        }
        showHaku();
    }

    public void showHaku() {
        breadcrumb.add(I18N.getMessage("tulosVerticalLayout.breadcrumb.haku", hakuDetails.getHakuOid()), new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                for (HakuRefreshEventListener l : hakuRefreshEventListeners) {
                    l.hakuRefresh();
                }
            }
        });

        mainLayout.removeAllComponents();
        hakuDetails.refresh();
        mainLayout.addComponent(hakuDetails);
    }

    public void showSijoitteluajo(Long sijoitteluajoId) {
        if (sijoitteluajoDetails == null) {
            sijoitteluajoDetails = new SijoitteluajoDetails(sijoitteluajoId, hakukohdeRefreshEventListeners);
        } else {
            sijoitteluajoDetails.setSijoitteluajoId(sijoitteluajoId);
        }

        showSijoitteluajo();
    }

    public void showSijoitteluajo() {
        breadcrumb.add(I18N.getMessage("tulosVerticalLayout.breadcrumb.sijoitteluajo", sijoitteluajoDetails.getSijoitteluajoId()), new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                for (SijoitteluajoRefreshEventListener l : sijoitteluajoRefreshEventListeners) {
                    l.sijoitteluajoRefresh();
                }
            }
        });

        mainLayout.removeAllComponents();
        sijoitteluajoDetails.refresh();
        mainLayout.addComponent(sijoitteluajoDetails);
    }

    public void showHakukohde(Long sijoitteluajoId, String hakukohdeOid) {
        if (hakukohdeDetails == null) {
            hakukohdeDetails = new HakukohdeDetails(sijoitteluajoId, hakukohdeOid);
        } else {
            hakukohdeDetails.setSijoitteluajoId(sijoitteluajoId);
            hakukohdeDetails.setHakukohdeOid(hakukohdeOid);
        }

        showHakukohde();
    }

    public void showHakukohde() {
        breadcrumb.add(I18N.getMessage("tulosVerticalLayout.breadcrumb.hakukohde", hakukohdeDetails.getHakukohdeOid()), new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                for (HakukohdeRefreshEventListener l : hakukohdeRefreshEventListeners) {
                    l.hakukohdeRefresh();
                }
            }
        });

        mainLayout.removeAllComponents();
        hakukohdeDetails.refresh();
        mainLayout.addComponent(hakukohdeDetails);
    }

    public void addListener(HakuRefreshEventListener hakuRefreshEventListener) {
        hakuRefreshEventListeners.add(hakuRefreshEventListener);

    }

    public void addListener(SijoitteluajoRefreshEventListener sijoitteluajoRefreshEventListener) {
        sijoitteluajoRefreshEventListeners.add(sijoitteluajoRefreshEventListener);

    }

    public void addListener(HakuListingRefreshEventListener hakuListingRefreshEventListener) {
        hakuListingRefreshEventListeners.add(hakuListingRefreshEventListener);
    }

    public void addListener(HakukohdeRefreshEventListener hakukohdeRefreshEventListener) {
        hakukohdeRefreshEventListeners.add(hakukohdeRefreshEventListener);

    }
}
