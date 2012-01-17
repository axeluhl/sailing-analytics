package com.sap.sailing.gwt.ui.racinganalysis;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;

public class RacingAnalysisPage extends AbstractEntryPoint {
    public void onModuleLoad() {
        super.onModuleLoad();
        // defines FlowPanel
        FlowPanel outerPanel = new FlowPanel(); // outer div which centered page content
        outerPanel.addStyleName("outerPanel");
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringConstants);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        RootPanel.get().add(logoAndTitlePanel);
        RootPanel.get().add(outerPanel);
    }
}
