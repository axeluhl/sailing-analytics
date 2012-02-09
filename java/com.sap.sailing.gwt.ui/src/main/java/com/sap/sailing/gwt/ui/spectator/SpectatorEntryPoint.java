package com.sap.sailing.gwt.ui.spectator;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.panels.SimpleWelcomeWidget;

/**
 * 
 * @author Lennart Hensler (D054527)
 *
 */
public class SpectatorEntryPoint extends AbstractEntryPoint implements EventRefresher {
    
    @Override
    public void onModuleLoad() {
        super.onModuleLoad();

        String groupParamValue = Window.Location.getParameter("leaderboardGroupName");
        final String groupName;
        if(groupParamValue == null || groupParamValue.isEmpty()) {
            groupName = null;
        } else {
            groupName = groupParamValue;
            sailingService.getLeaderboardGroupByName(groupName, new AsyncCallback<LeaderboardGroupDTO>() {
                @Override
                public void onFailure(Throwable t) {
                    reportError(stringMessages.noLeaderboardGroupWithNameFound(groupName));
                }
                @Override
                public void onSuccess(LeaderboardGroupDTO group) {}
            });
        }
        
        String root = Window.Location.getParameter("root");
        //Check if the root contains an allowed value
        if (root != null) {
            root = (root.equals("leaderboardGroupPanel") || root.equals("overview")) ? root : null;
        }
        RootPanel rootPanel = RootPanel.get();
        
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        rootPanel.add(logoAndTitlePanel);
        
        FormPanel panelToDisplay = null;
        if (groupName == null) {
            //TODO Adjust the code after the OverviewEventPanel is ready for the leaderboard groups
//            panelToDisplay = new OverviewEventPanel(sailingService, this, this, stringMessages);
            Window.alert(stringMessages.noLeaderboardGroupToLoad() + ".");
        } else {
            panelToDisplay = new LeaderboardGroupPanel(sailingService, stringMessages, this, groupName, root);
            LeaderboardGroupPanel groupPanel = (LeaderboardGroupPanel) panelToDisplay;
            groupPanel.setWelcomeWidget(new SimpleWelcomeWidget(stringMessages.welcomeToSailingAnalytics(),
                    "Ipsum lorum\nHello World!"));
            rootPanel.add(panelToDisplay);
        }
//        rootPanel.add(panelToDisplay);

        fillEvents();
    }

    @Override
    public void fillEvents() {
        
    }
    
}
