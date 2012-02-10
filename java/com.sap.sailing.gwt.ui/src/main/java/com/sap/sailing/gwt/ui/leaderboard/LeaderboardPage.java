package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.panels.BreadcrumbPanel;


public class LeaderboardPage extends AbstractEntryPoint {
    private String leaderboardName;
    private String leaderboardGroupName;
    
    @Override
    public void onModuleLoad() {     
        
        super.onModuleLoad();
        sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                leaderboardName = Window.Location.getParameter("name");
                leaderboardGroupName = Window.Location.getParameter("group");
                if (leaderboardNames.contains(leaderboardName)) {
                    createLeaderboardPanel();
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

    private void createLeaderboardPanel()
    {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        
        // create the breadcrumb navigation
        ArrayList<Pair<String, String>> breadcrumbLinksData = new ArrayList<Pair<String, String>>();
        String debugParam = Window.Location.getParameter("gwt.codesvr");
    
        if(leaderboardGroupName != null) {
            String link = "/gwt/Spectator.html?leaderboardGroupName=" + leaderboardGroupName;
            if(debugParam != null && !debugParam.isEmpty())
                link += "&gwt.codesvr=" + debugParam;
            breadcrumbLinksData.add(new Pair<String, String>(link, leaderboardGroupName));
        }
        BreadcrumbPanel breadcrumbPanel = new BreadcrumbPanel(breadcrumbLinksData, leaderboardName.toUpperCase());
        LeaderboardPanel leaderboardPanel =  new LeaderboardPanel(sailingService, LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(true),
                /* preSelectedRace */ null, new CompetitorSelectionModel(/* hasMultiSelection */ true),
                new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */3000l),
                leaderboardName, leaderboardGroupName,
                LeaderboardPage.this, stringMessages, userAgentType);
        RootPanel.get().add(logoAndTitlePanel);
        RootPanel.get().add(breadcrumbPanel);
        RootPanel.get().add(leaderboardPanel);
    }    
}
