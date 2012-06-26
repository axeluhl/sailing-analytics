package com.sap.sailing.gwt.ui.leaderboard;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;


public class LeaderboardEntryPoint extends AbstractEntryPoint {
    private String leaderboardName;
    private String leaderboardGroupName;
    
    @Override
    public void onModuleLoad() {     
        super.onModuleLoad();
        final boolean showRaceDetails = Window.Location.getParameter("showRaceDetails") != null
                && Window.Location.getParameter("showRaceDetails").equalsIgnoreCase("true");
        final boolean embedded = Window.Location.getParameter("embedded") != null
                && Window.Location.getParameter("embedded").equalsIgnoreCase("true");
        sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                leaderboardName = Window.Location.getParameter("name");
                leaderboardGroupName = Window.Location.getParameter("leaderboardGroupName");
                if (leaderboardNames.contains(leaderboardName)) {
                    createUI(showRaceDetails, embedded);
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
    
    private void createUI(boolean showRaceDetails, boolean embedded) {
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(mainPanel);
        LogoAndTitlePanel logoAndTitlePanel = null;
        if (!embedded) {
            logoAndTitlePanel = new LogoAndTitlePanel(leaderboardGroupName,
                    stringMessages.leaderboard() + " " + leaderboardName, stringMessages);
            logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
            mainPanel.addNorth(logoAndTitlePanel, 68);
        }
        ScrollPanel contentScrollPanel = new ScrollPanel();
        
        String tvModeParam = Window.Location.getParameter("tvMode");
        if (tvModeParam != null) {
            Timer timer = new Timer(PlayModes.Replay, 1000l);
            timer.setLivePlayDelayInMillis(5000l);
            TVViewPanel tvViewPanel = new TVViewPanel(sailingService, stringMessages, this, leaderboardName,
                    userAgentType, null, timer, logoAndTitlePanel, mainPanel, showRaceDetails);
            contentScrollPanel.setWidget(tvViewPanel);
        } else {
            Timer timer = new Timer(PlayModes.Replay, /* delayBetweenAutoAdvancesInMilliseconds */3000l);
            timer.setLivePlayDelayInMillis(5000l);
            LeaderboardPanel leaderboardPanel =  new LeaderboardPanel(sailingService, new AsyncActionsExecutor(),
                    LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, null, /* autoExpandFirstRace */ false),
                    /* preSelectedRace */ null, new CompetitorSelectionModel(/* hasMultiSelection */ true), timer,
                    leaderboardName, leaderboardGroupName,
                    LeaderboardEntryPoint.this, stringMessages, userAgentType, showRaceDetails);
            contentScrollPanel.setWidget(leaderboardPanel);
        }

        mainPanel.add(contentScrollPanel);
    }
    
}
