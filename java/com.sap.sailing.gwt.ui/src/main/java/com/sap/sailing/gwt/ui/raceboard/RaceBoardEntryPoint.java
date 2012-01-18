package com.sap.sailing.gwt.ui.raceboard;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;

public class RaceBoardEntryPoint extends AbstractEntryPoint {
    @Override
    public void onModuleLoad() {     
        super.onModuleLoad();
        sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                String leaderboardName = Window.Location.getParameter("name");
                    LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages);
                    logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
                    RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, leaderboardName,
                            RaceBoardEntryPoint.this, stringMessages);
                    
                    //String padding = Window.Location.getParameter("padding");
                    /*if (padding != null && Boolean.valueOf(padding)) {
                        raceBoardPanel.addStyleName("leftPaddedPanel");
                    }*/
                    
                    FlowPanel contentOuterPanel = new FlowPanel(); // outer div which centered page content
                    contentOuterPanel.addStyleName("contentOuterPanel");
                    contentOuterPanel.add(raceBoardPanel);
                    
                    FlowPanel timelinePanel = new FlowPanel();
                    timelinePanel.addStyleName("timelinePanel");
                    
                    FlowPanel timelineInnerPanel = new FlowPanel();
                    timelineInnerPanel.addStyleName("timelineInnerPanel");
                    
                    FlowPanel footerShadowPanel = new FlowPanel();
                    footerShadowPanel.addStyleName("footerShadowPanel");
                    
                    FlowPanel breadcrumbPanel = new FlowPanel();
                    breadcrumbPanel.addStyleName("breadcrumbPanel");
                    
                    timelinePanel.add(timelineInnerPanel);
                    
                    RootPanel.get().add(breadcrumbPanel);
                    RootPanel.get().add(contentOuterPanel);
                    
                    // Don't change this order because of the inner logic in html of "position fixed"-elements
                    RootPanel.get().add(logoAndTitlePanel);                 // position:fixed        
                    RootPanel.get().add(timelinePanel);                     // position:fixed
                    RootPanel.get().add(footerShadowPanel);                 // position:fixed
            }

            @Override
            public void onFailure(Throwable t) {
                reportError("Error trying to obtain list of leaderboard names: " + t.getMessage());
            }
        });
    }

}
