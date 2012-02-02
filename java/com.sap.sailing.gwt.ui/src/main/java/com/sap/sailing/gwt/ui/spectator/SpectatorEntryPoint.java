package com.sap.sailing.gwt.ui.spectator;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.components.ClosableWelcomeWidget;
import com.sap.sailing.gwt.ui.shared.components.SimpleWelcomeWidget;

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
        
        RootPanel rootPanel = RootPanel.get();
        rootPanel.setSize("100%", "100%");
        
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        rootPanel.add(logoAndTitlePanel);
        
        //TODO Create LeaderboardGroupPanel if groupName is filled
        FormPanel panelToDisplay = null;
        if (groupName == null) {
            panelToDisplay = new OverviewEventPanel(sailingService, this, this, stringMessages);
        } else {
            panelToDisplay = new LeaderboardGroupPanel(sailingService, stringMessages, this, groupName);
            LeaderboardGroupPanel groupPanel = (LeaderboardGroupPanel) panelToDisplay;
            groupPanel.setWelcomeWidget(new ClosableWelcomeWidget(true, stringMessages.welcomeToSailingAnalytics(),
                    "Ipsum lorum\nHello World!", SimpleWelcomeWidget.ALIGN_RIGHT, stringMessages));
        }
        panelToDisplay.setSize("100%", "100%");
        rootPanel.add(panelToDisplay);

        fillEvents();
    }

    @Override
    public void fillEvents() {
        
    }
    
}
