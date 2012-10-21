package com.sap.sailing.gwt.ui.tv;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;

public class TVEntryPoint extends AbstractEntryPoint {
    private static final String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardGroupName";
    private static final String PARAM_EMBEDDED = "embedded";
    private static final String PARAM_SHOW_RACE_DETAILS = "showRaceDetails";
    private static final String PARAM_DELAY_TO_LIVE_MILLIS = "delayToLiveMillis";
    private String leaderboardName;
    private String leaderboardGroupName;
    private TVViewController tvViewController;
    
    @Override
    public void onModuleLoad() {     
        super.onModuleLoad();
        final boolean showRaceDetails = Window.Location.getParameter(PARAM_SHOW_RACE_DETAILS) != null
                && Window.Location.getParameter(PARAM_SHOW_RACE_DETAILS).equalsIgnoreCase("true");
        final boolean embedded = Window.Location.getParameter(PARAM_EMBEDDED) != null
                && Window.Location.getParameter(PARAM_EMBEDDED).equalsIgnoreCase("true");
        final long delayToLiveMillis = Window.Location.getParameter(PARAM_DELAY_TO_LIVE_MILLIS) != null ?
                Long.valueOf(Window.Location.getParameter(PARAM_DELAY_TO_LIVE_MILLIS)) : 5000l; // default 5s
        sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                leaderboardName = Window.Location.getParameter("name");
                leaderboardGroupName = Window.Location.getParameter(PARAM_LEADERBOARD_GROUP_NAME);
                if (leaderboardNames.contains(leaderboardName)) {
                    createUI(showRaceDetails, embedded, delayToLiveMillis);
                } else {
                    RootPanel.get().add(new Label(stringMessages.noSuchLeaderboard()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                reportError("Error trying to obtain list of leaderboard names: " + t.getMessage());
            }
        });
    }
    
    private void createUI(boolean showRaceDetails, boolean embedded, long delayToLiveMillis) {
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(mainPanel);
        LogoAndTitlePanel logoAndTitlePanel = null;
        if (!embedded) {
            String leaderboardDisplayName = Window.Location.getParameter("displayName");
            if(leaderboardDisplayName == null || leaderboardDisplayName.isEmpty()) {
                leaderboardDisplayName = leaderboardName;
            }
            logoAndTitlePanel = new LogoAndTitlePanel(leaderboardGroupName, leaderboardDisplayName, stringMessages);
            logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
            mainPanel.addNorth(logoAndTitlePanel, 68);
        }
        
        tvViewController = new TVViewController(sailingService, stringMessages, this, leaderboardName,
                userAgent, logoAndTitlePanel, mainPanel, delayToLiveMillis, showRaceDetails);
        tvViewController.updateTvView(TVViews.Leaderboard);
    }
}
