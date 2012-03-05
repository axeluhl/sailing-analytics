package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.ComponentViewer;

@SuppressWarnings("deprecation")
public class SideBySideComponentViewer implements ComponentViewer {
    /** there is no easy replacement for the HorizontalSplitPanel available */ 
    private final HorizontalSplitPanel mainPanel;

    public SideBySideComponentViewer(Component<?> leftComponent, Component<?> rightComponent, String defaultWidth, String defaultHeight) {
        mainPanel = new HorizontalSplitPanel();
        mainPanel.setSize(defaultWidth, defaultHeight);
        mainPanel.setLeftWidget(leftComponent.getEntryWidget());
        mainPanel.setRightWidget(rightComponent.getEntryWidget());
    }

    public Panel getViewerWidget() {
        return mainPanel;
    }

    public Component<?> getRootComponent() {
        return null;
    }

    public String getViewerName() {
        return "";
    }
}
