package fi.vm.sade.sijoittelu.ui;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.DateHelper;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.sijoittelu.ui.event.SijoitteluajoRefreshEventListener;
import fi.vm.sade.sijoittelu.ui.service.TulosUiService;
import fi.vm.sade.tulos.service.types.tulos.HakuTyyppi;
import fi.vm.sade.tulos.service.types.tulos.SijoitteluajoTyyppi;
import fi.vm.sade.vaadin.Oph;

@SuppressWarnings("serial")
@Configurable(preConstruction = true)
public class HakuDetails extends VerticalLayout {

    @Autowired
    private TulosUiService tulosUiService;

    private Table sijoitteluajoTable;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private List<SijoitteluajoRefreshEventListener> sijoitteluajoChangeEventListeners;

    private String hakuOid;

    public HakuDetails(String hakuOid, List<SijoitteluajoRefreshEventListener> sijoitteluajoChangeEventListeners) {
        this.sijoitteluajoChangeEventListeners = sijoitteluajoChangeEventListeners;
        sijoitteluajoTable = createSijoitteluajoTable();
        this.hakuOid = hakuOid;
        addComponent(sijoitteluajoTable);
        refresh();
    }

    public void setHakuOid(String hakuOid) {
        this.hakuOid = hakuOid;
    }

    public String getHakuOid() {
        return this.hakuOid;
    }

    public void refresh() {
        sijoitteluajoTable.removeAllItems();

        final HakuTyyppi haku = tulosUiService.getHaku(hakuOid);
        List<SijoitteluajoTyyppi> sijoitteluajos = tulosUiService.getSijoitteluajos(haku.getSijoitteluajoIds());
        if (sijoitteluajos.isEmpty()) {
            return;
        }

        for (final SijoitteluajoTyyppi s : sijoitteluajos) {
            Button idButton = new Button(String.valueOf(s.getSijoitteluId()));
            idButton.setStyleName(Oph.BUTTON_LINK);
            idButton.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    for (SijoitteluajoRefreshEventListener l : sijoitteluajoChangeEventListeners) {
                        l.sijoitteluajoRefresh(s.getSijoitteluId());
                    }
                }
            });

            Object[] tableLine = new Object[] { idButton, s.getAloitusaika() != null ? sdf.format(DateHelper.xmlCalToDate(s.getAloitusaika())) : null,
                    s.getPaattymisaika() != null ? sdf.format(DateHelper.xmlCalToDate(s.getPaattymisaika())) : null };
            sijoitteluajoTable.addItem(tableLine, s);
        }
    }

    private Table createSijoitteluajoTable() {
        Table table = new Table();
        table.addContainerProperty(I18N.getMessage("hakuDetails.sijoitteluajo.id"), Button.class, null);
        table.addContainerProperty(I18N.getMessage("hakuDetails.sijoitteluajo.aloitusaika"), String.class, null);
        table.addContainerProperty(I18N.getMessage("hakuDetails.sijoitteluajo.paattymisaika"), String.class, null);
        return table;
    }
}
