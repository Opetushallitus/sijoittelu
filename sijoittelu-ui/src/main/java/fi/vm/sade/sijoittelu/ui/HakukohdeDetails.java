package fi.vm.sade.sijoittelu.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.sijoittelu.ui.service.TulosUiService;
import fi.vm.sade.tulos.service.types.tulos.HakijaTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakijanTilaTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakukohdeTyyppi;
import fi.vm.sade.tulos.service.types.tulos.ValintatapajonoTyyppi;

@SuppressWarnings("serial")
@Configurable(preConstruction = true)
public class HakukohdeDetails extends VerticalLayout {

    @Autowired
    private TulosUiService tulosUiService;

    private VerticalLayout hakukohdeHeaderLayout;
    private VerticalLayout valintatapajonosLayout;
    private String hakukohdeOid;

    private Long sijoitteluajoId;

    public HakukohdeDetails(Long sijoitteluajoId, String hakukohdeOid) {
        this.hakukohdeOid = hakukohdeOid;
        this.sijoitteluajoId = sijoitteluajoId;
        hakukohdeHeaderLayout = new VerticalLayout();
        valintatapajonosLayout = new VerticalLayout();
        addComponent(hakukohdeHeaderLayout);
        addComponent(valintatapajonosLayout);
        refresh();
    }

    public void setHakukohdeOid(String hakukohdeOid) {
        this.hakukohdeOid = hakukohdeOid;
    }

    public void setSijoitteluajoId(Long sijoitteluajoId) {
        this.sijoitteluajoId = sijoitteluajoId;
    }

    public String getHakukohdeOid() {
        return this.hakukohdeOid;
    }

    public Long getSijoitteluajoId() {
        return this.sijoitteluajoId;
    }

    public void refresh() {
        hakukohdeHeaderLayout.removeAllComponents();
        valintatapajonosLayout.removeAllComponents();

        HakukohdeTyyppi hakukohde = tulosUiService.getHakukohde(sijoitteluajoId, hakukohdeOid);
        hakukohdeHeaderLayout.addComponent(createHakukohdeHeader(hakukohde));
        valintatapajonosLayout.addComponent(createValintatapajonoTables(hakukohde.getValintatapajonos()));
    }

    private Component createValintatapajonoTables(List<ValintatapajonoTyyppi> valintatapajonos) {
        Collections.sort(valintatapajonos, new Comparator<ValintatapajonoTyyppi>() {

            @Override
            public int compare(ValintatapajonoTyyppi o1, ValintatapajonoTyyppi o2) {
                if (o1.getPrioriteetti() < o2.getPrioriteetti()) {
                    return -1;
                } else if (o1.getPrioriteetti() > o2.getPrioriteetti()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        TabSheet tabSheet = new TabSheet();
        for (ValintatapajonoTyyppi valintatapajono : valintatapajonos) {
            VerticalLayout jonoLayout = new VerticalLayout();
            jonoLayout.addComponent(createValintatapajonoHeader(valintatapajono));
            jonoLayout.addComponent(createValintatapajonoLayout(valintatapajono));
            tabSheet.addTab(jonoLayout, I18N.getMessage("hakukohdeDetails.valintatapajono", valintatapajono.getOid()));
        }

        return tabSheet;
    }

    private Component createValintatapajonoHeader(ValintatapajonoTyyppi valintatapajono) {
        GridLayout gridLayout = new GridLayout(2, 2);

        Label oidCaption = new Label(I18N.getMessage("hakukohdeDetails.valintatapajono.oid"));
        Label oid = new Label(valintatapajono.getOid());

        Label prioriteettiCaption = new Label(I18N.getMessage("hakukohdeDetails.valintatapajono.prioriteetti"));
        Label prioriteetti = new Label(String.valueOf(valintatapajono.getPrioriteetti()));

        gridLayout.addComponent(oidCaption);
        gridLayout.addComponent(oid);
        gridLayout.setComponentAlignment(oidCaption, Alignment.MIDDLE_LEFT);
        gridLayout.setComponentAlignment(oid, Alignment.MIDDLE_RIGHT);

        gridLayout.addComponent(prioriteettiCaption);
        gridLayout.addComponent(prioriteetti);
        gridLayout.setComponentAlignment(prioriteettiCaption, Alignment.MIDDLE_LEFT);
        gridLayout.setComponentAlignment(prioriteetti, Alignment.MIDDLE_RIGHT);

        return gridLayout;
    }

    private Component createValintatapajonoLayout(final ValintatapajonoTyyppi valintatapajono) {
        VerticalLayout layout = new VerticalLayout();

        final Table table = new Table();
        table.addContainerProperty(I18N.getMessage("hakukohdeDetails.hakija.jonosija"), Integer.class, null);
        table.addContainerProperty(I18N.getMessage("hakukohdeDetails.hakija.oid"), String.class, null);
        table.addContainerProperty(I18N.getMessage("hakukohdeDetails.hakija.prioriteetti"), Integer.class, null);
        table.addContainerProperty(I18N.getMessage("hakukohdeDetails.hakija.pisteet"), Double.class, null);
        table.addContainerProperty(I18N.getMessage("hakukohdeDetails.hakija.tila"), String.class, null);

        final CheckBox naytaVainHyvaksytytCheckBox = new CheckBox(I18N.getMessage("hakukohdeDetails.valintatapajono.naytaVainHyvaksytyt"));
        naytaVainHyvaksytytCheckBox.setImmediate(true);
        naytaVainHyvaksytytCheckBox.setValue(true);
        naytaVainHyvaksytytCheckBox.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                if ((Boolean) naytaVainHyvaksytytCheckBox.getValue()) {
                    refreshHakijaTable(table, filterAndSortHakijas(valintatapajono.getHakijaList(), HakijanTilaTyyppi.HYVAKSYTTY));
                } else {
                    refreshHakijaTable(table, filterAndSortHakijas(valintatapajono.getHakijaList()));
                }
            }
        });

        refreshHakijaTable(table, filterAndSortHakijas(valintatapajono.getHakijaList(), HakijanTilaTyyppi.HYVAKSYTTY));

        layout.addComponent(naytaVainHyvaksytytCheckBox);
        layout.addComponent(table);
        return layout;
    }

    private void refreshHakijaTable(Table hakijaTable, List<HakijaTyyppi> hakijas) {
        hakijaTable.removeAllItems();

        for (HakijaTyyppi h : hakijas) {
            Object[] tableline = new Object[] { h.getJonosija(), h.getOid(), h.getPrioriteetti(), h.getPisteet(),
                    I18N.getMessage("hakija.tila." + h.getTila().name()) };
            hakijaTable.addItem(tableline, h);
        }
        hakijaTable.requestRepaint();
    }

    private List<HakijaTyyppi> filterAndSortHakijas(List<HakijaTyyppi> hakijas, HakijanTilaTyyppi... tilas) {
        if (tilas == null || tilas.length == 0) {
            return hakijas;
        }

        Set<HakijanTilaTyyppi> tilaSet = new HashSet<HakijanTilaTyyppi>(Arrays.asList(tilas));

        List<HakijaTyyppi> filtered = new ArrayList<HakijaTyyppi>();

        for (HakijaTyyppi h : hakijas) {
            if (tilaSet.contains(h.getTila())) {
                filtered.add(h);
            }
        }

        Collections.sort(filtered, new Comparator<HakijaTyyppi>() {

            @Override
            public int compare(HakijaTyyppi o1, HakijaTyyppi o2) {
                if (o1.getJonosija() < o2.getJonosija()) {
                    return -1;
                } else if (o1.getJonosija() > o2.getJonosija()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        return filtered;
    }

    private Component createHakukohdeHeader(HakukohdeTyyppi hakukohde) {
        GridLayout gridLayout = new GridLayout(2, 3);

        Label oidCaption = new Label(I18N.getMessage("hakukohdeDetails.hakukohde.oid"));
        Label oid = new Label(hakukohde.getOid());

        Label nimiCaption = new Label(I18N.getMessage("hakukohdeDetails.hakukohde.nimi"));
        Label nimi = new Label(hakukohde.getNimi());

        Label tilaCaption = new Label(I18N.getMessage("hakukohdeDetails.hakukohde.tila"));
        Label tila = new Label(hakukohde.getTila() != null ? I18N.getMessage("hakukohde.tila." + hakukohde.getTila().name()) : "-");

        gridLayout.addComponent(oidCaption);
        gridLayout.addComponent(oid);
        gridLayout.setComponentAlignment(oidCaption, Alignment.MIDDLE_LEFT);
        gridLayout.setComponentAlignment(oid, Alignment.MIDDLE_RIGHT);

        gridLayout.addComponent(nimiCaption);
        gridLayout.addComponent(nimi);
        gridLayout.setComponentAlignment(nimiCaption, Alignment.MIDDLE_LEFT);
        gridLayout.setComponentAlignment(nimi, Alignment.MIDDLE_RIGHT);

        gridLayout.addComponent(tilaCaption);
        gridLayout.addComponent(tila);
        gridLayout.setComponentAlignment(tilaCaption, Alignment.MIDDLE_LEFT);
        gridLayout.setComponentAlignment(tila, Alignment.MIDDLE_RIGHT);

        return gridLayout;
    }

}
