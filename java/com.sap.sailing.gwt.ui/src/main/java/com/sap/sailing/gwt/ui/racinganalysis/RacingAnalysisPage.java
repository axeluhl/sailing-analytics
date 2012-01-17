package com.sap.sailing.gwt.ui.racinganalysis;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;

public class RacingAnalysisPage extends AbstractEntryPoint {
    public void onModuleLoad() {
        super.onModuleLoad();
        
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringConstants);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        
        FlowPanel contentOuterPanel = new FlowPanel(); // outer div which centered page content
        contentOuterPanel.addStyleName("contentOuterPanel");
        
        FlowPanel timelinePanel = new FlowPanel();
        timelinePanel.addStyleName("timelinePanel");
        
        FlowPanel timelineInnerPanel = new FlowPanel();
        timelineInnerPanel.addStyleName("timelineInnerPanel");
        
        FlowPanel footerShadowPanel = new FlowPanel();
        footerShadowPanel.addStyleName("footerShadowPanel");
        
        timelinePanel.add(timelineInnerPanel);
        
        RootPanel.get().add(contentOuterPanel);
        
        // Don't change this order because of the inner logic in html of "position fixed"-elements
        RootPanel.get().add(logoAndTitlePanel);                 // position:fixed        
        RootPanel.get().add(timelinePanel);                     // position:fixed
        RootPanel.get().add(footerShadowPanel);                 // position:fixed
    }
}
