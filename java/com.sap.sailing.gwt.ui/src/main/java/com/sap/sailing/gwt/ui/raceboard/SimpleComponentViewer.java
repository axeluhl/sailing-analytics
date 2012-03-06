package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.ComponentViewer;

public class SimpleComponentViewer<SettingsType> implements ComponentViewer {
    private final SimplePanel mainPanel;
    private final Component<SettingsType> component;
    
    public SimpleComponentViewer(Component<SettingsType> component, String defaultWidth, String defaultHeight) {
        this.component = component;
        mainPanel = new SimplePanel();
        mainPanel.setSize(defaultWidth, defaultHeight);
        mainPanel.setWidget(component.getEntryWidget());
    }

    public Panel getViewerWidget() {
        return mainPanel;
    }

    public Component<?> getRootComponent() {
        return component;
    }

    public String getViewerName() {
        return component.getLocalizedShortName();
    }
}
