package fi.vm.sade.sijoittelu.ui.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import fi.vm.sade.vaadin.Oph;

@SuppressWarnings("serial")
public class Breadcrumb extends HorizontalLayout {

    private List<Button> breadcrumbButtons = new ArrayList<Button>();

    public Breadcrumb() {
    }

    public void add(String caption) {
        add(caption, (List<ClickListener>) null);
    }

    public void add(String caption, ClickListener... listeners) {
        add(caption, Arrays.asList(listeners));
    }

    public void add(String caption, List<ClickListener> listeners) {
        Button button = new Button(caption);
        button.setStyleName(Oph.BUTTON_LINK);

        if (listeners != null && !listeners.isEmpty()) {
            for (ClickListener l : listeners) {
                button.addListener(l);
            }
        }

        button.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                int index = -1;
                for (int i = 0; i < breadcrumbButtons.size(); ++i) {
                    if (breadcrumbButtons.get(i).equals(event.getSource())) {
                        index = i;
                        break;
                    }
                }

                if (index < 0) {
                    throw new RuntimeException("Could not find button in breadcrumb list.");
                }

                while (breadcrumbButtons.size() > index + 1) {
                    breadcrumbButtons.remove(breadcrumbButtons.size() - 1);
                }

                refresh();
            }
        });

        breadcrumbButtons.add(button);
        refresh();
    }

    public void refresh() {
        removeAllComponents();
        for (int i = 0; i < breadcrumbButtons.size(); ++i) {
            if (i > 0) {
                Label l = new Label(">");
                addComponent(l);
                l.setStyleName(Oph.SPACING_RIGHT_10);
                setComponentAlignment(l, Alignment.MIDDLE_CENTER);
            }
            Button b = breadcrumbButtons.get(i);
            addComponent(b);
            setComponentAlignment(b, Alignment.MIDDLE_CENTER);
        }
    }

}
