package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;

public class BusyDialog extends DialogBox {

    private final FlowPanel mainPanel;
    private final SimpleBusyIndicator busyIndicator;
    
    public BusyDialog() {
        this("");
    }
    
    public BusyDialog(String title) {
        busyIndicator = new SimpleBusyIndicator(true, 1.5f);
        mainPanel = new FlowPanel();
        mainPanel.setWidth("100%");
        mainPanel.setPixelSize(50, 50);
        mainPanel.add(busyIndicator);
        center();
        setTitle(title);
        setModal(true);
        setGlassEnabled(true);
        setWidget(mainPanel);
    }
    
}
