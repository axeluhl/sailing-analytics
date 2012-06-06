package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.panels.BreadcrumbPanel;


public class LeaderboardEntryPoint extends AbstractEntryPoint {
    private String leaderboardName;
    private String leaderboardGroupName;
    
    @Override
    public void onModuleLoad() {     
        super.onModuleLoad();
        sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                leaderboardName = Window.Location.getParameter("name");
                leaderboardGroupName = Window.Location.getParameter("leaderboardGroupName");
                if (leaderboardNames.contains(leaderboardName)) {
                    createUI();
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
    
    private void createUI() {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(leaderboardName, stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");

        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(mainPanel);
        mainPanel.addNorth(logoAndTitlePanel, 68);
        
        ScrollPanel contentScrollPanel = new ScrollPanel();
        BreadcrumbPanel breadcrumbPanel = null;
        
        String tvModeParam = Window.Location.getParameter("tvMode");
        if (tvModeParam != null) {
            Timer timer = new Timer(PlayModes.Replay, 1000l);
            timer.setLivePlayDelayInMillis(5000l);
            TVViewPanel tvViewPanel = new TVViewPanel(sailingService, stringMessages, this, leaderboardName,
                    userAgentType, null, timer, logoAndTitlePanel, mainPanel);
            contentScrollPanel.setWidget(tvViewPanel);
        } else {
            Timer timer = new Timer(PlayModes.Replay, /* delayBetweenAutoAdvancesInMilliseconds */3000l);
            timer.setLivePlayDelayInMillis(5000l);
            LeaderboardPanel leaderboardPanel =  new LeaderboardPanel(sailingService, new AsyncActionsExecutor(),
                    LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, null, /* autoExpandFirstRace */ false),
                    /* preSelectedRace */ null, new CompetitorSelectionModel(/* hasMultiSelection */ true), timer,
                    leaderboardName, leaderboardGroupName,
                    LeaderboardEntryPoint.this, stringMessages, userAgentType);
            contentScrollPanel.setWidget(leaderboardPanel);
            breadcrumbPanel = createBreadcrumbPanel();
        }
        if (breadcrumbPanel != null) {
            mainPanel.addNorth(breadcrumbPanel, 30);
        }
        mainPanel.add(contentScrollPanel);
    }
    
    private BreadcrumbPanel createBreadcrumbPanel() {
        ArrayList<Pair<String, String>> breadcrumbLinksData = new ArrayList<Pair<String, String>>();
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        if(leaderboardGroupName != null) {
            if (Window.Location.getParameter("root").equals("overview")) {
                String link = "/gwt/Spectator.html"
                        + (debugParam != null && !debugParam.isEmpty() ? "?gwt.codesvr=" + debugParam : "");
                breadcrumbLinksData.add(new Pair<String, String>(link, stringMessages.home()));
            }
            String link = "/gwt/Spectator.html?leaderboardGroupName=" + leaderboardGroupName
                    + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : "");
            breadcrumbLinksData.add(new Pair<String, String>(link, leaderboardGroupName));
        }
        return new BreadcrumbPanel(breadcrumbLinksData, leaderboardName.toUpperCase());
    }
}
