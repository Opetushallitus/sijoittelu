package fi.vm.sade.sijoittelu.ui;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.sijoittelu.ui.event.HakuRefreshEventListener;
import fi.vm.sade.sijoittelu.ui.service.TulosUiService;
import fi.vm.sade.tulos.service.types.tulos.HakuTyyppi;
import fi.vm.sade.vaadin.Oph;

@SuppressWarnings("serial")
@Configurable(preConstruction = true)
public class HakuListing extends VerticalLayout {

    @Autowired
    private TulosUiService tulosUiService;
    private Table hakuTable;

    private List<HakuRefreshEventListener> hakuChangeEventListeners;

    public HakuListing(List<HakuRefreshEventListener> hakuChangeEventListeners) {
        this.hakuChangeEventListeners = hakuChangeEventListeners;
        hakuTable = createHakuTable();

        addComponent(hakuTable);
        refresh();
    }

    public void refresh() {
        hakuTable.removeAllItems();
        List<HakuTyyppi> hakus = tulosUiService.listHakus();

        if (hakus.isEmpty()) {
            return;
        }

        for (final HakuTyyppi haku : hakus) {
            Button oidButton = new Button(haku.getOid());
            oidButton.setStyleName(Oph.BUTTON_LINK);
            oidButton.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    for (HakuRefreshEventListener l : hakuChangeEventListeners) {
                        l.hakuRefresh(haku.getOid());
                    }
                }
            });

            Object[] tableLine = new Object[] { oidButton, haku.getHaunTunniste() };
            hakuTable.addItem(tableLine, haku);
        }
    }

    private Table createHakuTable() {
        Table hakutable = new Table();
        hakutable.addContainerProperty(I18N.getMessage("hakuListing.haku.oid"), Button.class, null);
        hakutable.addContainerProperty(I18N.getMessage("hakuListing.haku.haunTunniste"), String.class, null);

        hakutable.setWidth("100%");
        hakutable.setSizeFull();
        hakutable.setPageLength(0);
        return hakutable;
    }

}
