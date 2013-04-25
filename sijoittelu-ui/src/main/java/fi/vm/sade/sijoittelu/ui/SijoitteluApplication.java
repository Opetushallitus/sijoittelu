package fi.vm.sade.sijoittelu.ui;

import com.vaadin.Application;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.app.AbstractSadePortletApplication;
import fi.vm.sade.sijoittelu.ui.event.HakuRefreshEventListener;
import fi.vm.sade.sijoittelu.ui.event.HakuListingRefreshEventListener;
import fi.vm.sade.sijoittelu.ui.event.HakukohdeRefreshEventListener;
import fi.vm.sade.sijoittelu.ui.event.SijoitteluajoRefreshEventListener;

@SuppressWarnings("serial")
public class SijoitteluApplication extends AbstractSadePortletApplication {

    public SijoitteluApplication() {
        super();
    }

    private static ThreadLocal<SijoitteluApplication> tl = new ThreadLocal<SijoitteluApplication>();

    @Override
    public void transactionStart(Application application, Object transactionData) {
        super.transactionStart(application, transactionData);
        if (application == this) {
            tl.set(this);
        }
    }

    @Override
    public void transactionEnd(Application application, Object transactionData) {
        super.transactionEnd(application, transactionData);
        if (application == this) {
            tl.remove();
        }
    }

    public static SijoitteluApplication getInstance() {
        return tl.get();
    }

    @Override
    public synchronized void init() {
        super.init();
        setTheme("oph");
        getContext().addTransactionListener(this);
        this.transactionStart(this, null); // run transaction start for the
                                           // first time, otherwise creating
                                           // windows will fail
        createMainWindow();
    }

    private void createMainWindow() {
        HorizontalLayout layout = new HorizontalLayout();

        Window mainWindow = new Window(I18N.getMessage("sijoitteluApplication.title"), layout);
        setMainWindow(mainWindow);

        final TulosVerticalLayout tulosVerticalLayout = new TulosVerticalLayout();

        HakuListingRefreshEventListener hakuDetailsBackButtonClickEventListener = new HakuListingRefreshEventListener() {
            @Override
            public void refreshHakuListing() {
                tulosVerticalLayout.listHakus();
            }
        };

        HakuRefreshEventListener hakuChangeEventListener = new HakuRefreshEventListener() {
            @Override
            public void hakuRefresh(String hakuOid) {
                tulosVerticalLayout.showHaku(hakuOid);
            }

            @Override
            public void hakuRefresh() {
                tulosVerticalLayout.showHaku();
            }
        };

        SijoitteluajoRefreshEventListener sijoitteluajoChangeEventListener = new SijoitteluajoRefreshEventListener() {

            @Override
            public void sijoitteluajoRefresh(Long sijoitteluajoId) {
                tulosVerticalLayout.showSijoitteluajo(sijoitteluajoId);
            }

            @Override
            public void sijoitteluajoRefresh() {
                tulosVerticalLayout.showSijoitteluajo();
            }
        };

        HakukohdeRefreshEventListener hakukohdeChangeEventListener = new HakukohdeRefreshEventListener() {

            @Override
            public void hakukohdeRefresh(Long sijoitteluajoId, String hakukohdeOid) {
                tulosVerticalLayout.showHakukohde(sijoitteluajoId, hakukohdeOid);

            }

            @Override
            public void hakukohdeRefresh() {
                tulosVerticalLayout.showHakukohde();
            }
        };

        tulosVerticalLayout.addListener(hakuDetailsBackButtonClickEventListener);
        tulosVerticalLayout.addListener(hakuChangeEventListener);
        tulosVerticalLayout.addListener(sijoitteluajoChangeEventListener);
        tulosVerticalLayout.addListener(hakukohdeChangeEventListener);

        layout.addComponent(tulosVerticalLayout);
    }

}
