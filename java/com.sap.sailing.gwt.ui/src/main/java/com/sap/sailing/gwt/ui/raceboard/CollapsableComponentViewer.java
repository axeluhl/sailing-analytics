package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.components.CollapsablePanel;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.ComponentGroup;
import com.sap.sailing.gwt.ui.shared.components.ComponentToolbar;

/**
 * A GWT component that visualizes a {@link Component} or a {@link Component} including menus to scroll quickly to the embedded view
 * of the respective component, collapse/expand buttons for the views embedded and homogeneous settings buttons for
 * those components that have settings.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CollapsableComponentViewer<SettingsType> {
    private final CollapsablePanel disclosurePanel;
    
    private final Component<SettingsType> component;

    private final StringMessages stringMessages;
    
    public CollapsableComponentViewer(Component<SettingsType> component, StringMessages stringMessages) {
        this.component = component;
        this.stringMessages = stringMessages;
        
        AbsolutePanel contentPanel = new AbsolutePanel();
        disclosurePanel = createDisclosePanel(contentPanel, component.getLocalizedShortName(), 150); 
    }

    public Widget getViewerWidget() {
        return disclosurePanel;
    }

    public Component<?> getComponent() {
        return component;
    }

    private CollapsablePanel createDisclosePanel(Panel contentPanel, String panelTitle, int heightInPx)
    {
        CollapsablePanel disclosurePanel = new CollapsablePanel (panelTitle);
        disclosurePanel.setSize("100%", "100%");
        disclosurePanel.setOpen(true);
        
        ComponentToolbar<SettingsType> toolbar = new ComponentToolbar<SettingsType>(component, stringMessages);
        toolbar.addSettingsButton();
        disclosurePanel.setHeaderToolbar(toolbar);
        
        contentPanel.setSize("100%", heightInPx + "px");
        disclosurePanel.setContent(contentPanel);
        if(component.getEntryWidget() != null) {
            contentPanel.add(component.getEntryWidget());
        } else {
            if(component instanceof ComponentGroup) {
                Iterable<Component<?>> components = ((ComponentGroup<?>) component).getComponents();
                for(Component<?> c: components) {
                    if(c.getEntryWidget() != null)
                        contentPanel.add(c.getEntryWidget());
                }
            }
        }
        
        return disclosurePanel;
    }

}
