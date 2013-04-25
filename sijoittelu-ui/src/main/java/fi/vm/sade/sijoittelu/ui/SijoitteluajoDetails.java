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
import fi.vm.sade.sijoittelu.ui.event.HakukohdeRefreshEventListener;
import fi.vm.sade.sijoittelu.ui.service.TulosUiService;
import fi.vm.sade.tulos.service.types.tulos.HakukohdeTyyppi;
import fi.vm.sade.tulos.service.types.tulos.SijoitteluajoTyyppi;
import fi.vm.sade.vaadin.Oph;

@SuppressWarnings("serial")
@Configurable(preConstruction = true)
public class SijoitteluajoDetails extends VerticalLayout {

    @Autowired
    private TulosUiService tulosUiService;

    private List<HakukohdeRefreshEventListener> hakukohdeChangeEventListeners;
    private Table hakukohdeTable;

    private Long sijoitteluajoId;

    public SijoitteluajoDetails(Long sijoitteluajoId, List<HakukohdeRefreshEventListener> hakukohdeChangeEventListeners) {
        this.sijoitteluajoId = sijoitteluajoId;
        this.hakukohdeChangeEventListeners = hakukohdeChangeEventListeners;
        hakukohdeTable = createHakukohdeTable();
        addComponent(hakukohdeTable);
        refresh();
    }

    public void setSijoitteluajoId(Long sijoitteluajoId) {
        this.sijoitteluajoId = sijoitteluajoId;
    }

    public Long getSijoitteluajoId() {
        return this.sijoitteluajoId;
    }

    public void refresh() {
        hakukohdeTable.removeAllItems();

        SijoitteluajoTyyppi sijoitteluajo = tulosUiService.getSijoitteluajo(sijoitteluajoId);
        List<HakukohdeTyyppi> hakukohdes = tulosUiService.getHakukohdes(sijoitteluajoId, sijoitteluajo.getHakukohdeOids());
        for (final HakukohdeTyyppi h : hakukohdes) {
            Button oidButton = new Button(h.getOid());
            oidButton.setStyleName(Oph.BUTTON_LINK);
            oidButton.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    for (HakukohdeRefreshEventListener l : hakukohdeChangeEventListeners) {
                        l.hakukohdeRefresh(sijoitteluajoId, h.getOid());
                    }
                }
            });

            Object[] tableline = new Object[] { oidButton, h.getNimi(), h.getTila() != null ? I18N.getMessage("hakukohde.tila." + h.getTila().name()) : "-" };
            hakukohdeTable.addItem(tableline, h);
        }
    }

    private Table createHakukohdeTable() {
        Table table = new Table();
        table.addContainerProperty(I18N.getMessage("sijoitteluajoDetails.hakukohde.oid"), Button.class, null);
        table.addContainerProperty(I18N.getMessage("sijoitteluajoDetails.hakukohde.nimi"), String.class, null);
        table.addContainerProperty(I18N.getMessage("sijoitteluajoDetails.hakukohde.tila"), String.class, null);
        return table;
    }
}
