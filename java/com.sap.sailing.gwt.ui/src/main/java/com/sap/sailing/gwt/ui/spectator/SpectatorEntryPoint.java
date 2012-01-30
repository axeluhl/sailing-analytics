package com.sap.sailing.gwt.ui.spectator;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.AbstractEventPanel;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;

/**
 * 
 * @author Lennart Hensler (D054527)
 *
 */
public class SpectatorEntryPoint extends AbstractEntryPoint implements EventRefresher {
    
    @Override
    public void onModuleLoad() {
        super.onModuleLoad();

        //Fill fixed leaderboard selection
        String leaderboardNameParamValue = Window.Location.getParameter("leaderboardName");
        String eventNameParamValue = Window.Location.getParameter("eventName");
        @SuppressWarnings("unused")
        String isLiveParamValue = Window.Location.getParameter("isLive");
        
        //Forced to 'live' for Hawai-Event
        @SuppressWarnings("unused")
        boolean isLive = true;
//        Boolean isLive = (isLiveParamValue == null || isLiveParamValue.isEmpty()) ? null : Boolean.valueOf(isLiveParamValue);
       
        final String leaderboardName;
        if(leaderboardNameParamValue == null || leaderboardNameParamValue.isEmpty()) {
            leaderboardName = null;
        } else {
            leaderboardName = leaderboardNameParamValue;
            sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> leaderboardNames) {
                    if (!leaderboardNames.contains(leaderboardName)) {
                        createErrorPage(stringMessages.noSuchLeaderboard());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    reportError("Error trying to obtain the list of leaderboard names: " + t.getMessage());
                }
            });
        }
        
        String eventName = (eventNameParamValue == null || eventNameParamValue.isEmpty()) ? null : eventNameParamValue;
        
        RootPanel rootPanel = RootPanel.get();
        rootPanel.setSize("100%", "100%");
        
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        rootPanel.add(logoAndTitlePanel);
        
        AbstractEventPanel panelToDisplay = (leaderboardName != null && eventName != null) ? 
                new LiveEventViewPanel(sailingService, this, this, stringMessages, leaderboardName, eventName) :
                new OverviewEventPanel(sailingService, this, this, stringMessages);
        panelToDisplay.setSize("100%", "100%");
        rootPanel.add(panelToDisplay);

        fillEvents();
    }

    @Override
    public void fillEvents() {
        
    }
    
}
