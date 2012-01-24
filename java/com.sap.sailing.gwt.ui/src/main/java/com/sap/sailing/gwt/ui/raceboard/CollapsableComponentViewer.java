package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.components.CollapsablePanel;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.ComponentGroup;
import com.sap.sailing.gwt.ui.shared.components.ComponentToolbar;

/**
 * A GWT component that visualizes a {@link ComponentGroup} or a {@link Component} including menus to scroll quickly to the embedded view
 * of the respective component, collapse/expand buttons for the views embedded and homogeneous settings buttons for
 * those components that have settings.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CollapsableComponentViewer<SettingsType> {
    private final CollapsablePanel collapsablePanel;
    
    private final Component<SettingsType> component;

    private final StringMessages stringMessages;
    
    private boolean hasHeaderToolbar = false;
    
    public CollapsableComponentViewer(Component<SettingsType> component, String defaultWidth, String defaultHeight, StringMessages stringMessages) {
        this.component = component;
        this.stringMessages = stringMessages;
        
        AbsolutePanel contentPanel = new AbsolutePanel();
        collapsablePanel = createCollapsablePanel(contentPanel, component.getLocalizedShortName(), defaultWidth, defaultHeight); 
    }

    public CollapsablePanel getViewerWidget() {
        return collapsablePanel;
    }

    public Component<?> getComponent() {
        return component;
    }

    private CollapsablePanel createCollapsablePanel(Panel contentPanel, String panelTitle, String defaultContentWidth, String defaultContentHeight)
    {
        CollapsablePanel collapsablePanel = new CollapsablePanel (panelTitle);
        collapsablePanel.setSize("100%", "100%");
        collapsablePanel.setOpen(true);
        
        if(hasHeaderToolbar) {
            ComponentToolbar<SettingsType> toolbar = new ComponentToolbar<SettingsType>(component, stringMessages);
            toolbar.addSettingsButton();
            collapsablePanel.setHeaderToolbar(toolbar);
        }
        
        contentPanel.setSize(defaultContentWidth, defaultContentHeight);
        collapsablePanel.setContent(contentPanel);
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
        
        return collapsablePanel;
    }
}
