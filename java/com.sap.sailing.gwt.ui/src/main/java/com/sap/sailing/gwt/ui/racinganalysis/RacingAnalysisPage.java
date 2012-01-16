package com.sap.sailing.gwt.ui.racinganalysis;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;

public class RacingAnalysisPage extends AbstractEntryPoint {
    public void onModuleLoad() {
        // defines FlowPanel
        FlowPanel outerPanel = new FlowPanel(); // outer div which centered page content
        outerPanel.addStyleName("outerPanel");
        
        RootPanel.get().add(outerPanel);
    }
}
