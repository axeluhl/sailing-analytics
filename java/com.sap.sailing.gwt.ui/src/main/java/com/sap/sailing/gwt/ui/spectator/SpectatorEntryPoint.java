package com.sap.sailing.gwt.ui.spectator;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
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
        String viewModeParamValue = Window.Location.getParameter("viewMode");
        final String groupName;
        if (groupParamValue == null || groupParamValue.isEmpty()) {
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
        FlowPanel groupAndFeedbackPanel = new FlowPanel();
        
        if (groupName == null) {
            //TODO Enable code below, when the LeaderboardGroupOverviewPanel was styled
//            LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.overview(), stringMessages);
//            logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
//            panelToDisplay = new LeaderboardGroupOverviewPanel(sailingService, this, stringMessages);
            Window.alert("No leaderboard group name was given.");
        } else {
            LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(groupName, stringMessages);
            logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
            rootPanel.add(logoAndTitlePanel);
            
            LeaderboardGroupPanel groupPanel = new LeaderboardGroupPanel(sailingService, stringMessages, this, groupName, root, viewModeParamValue);
            groupPanel.setWelcomeWidget(new SimpleWelcomeWidget( stringMessages.welcomeToSailingAnalytics(),
                            "Understanding what happens out on the race course isn't always easy. To help solve this challenge and" +
                            " bring the excitement of sailing to the fans, we have developed a leader board based on SAP analytics.\n" +
                            " Through analyzing GPS data together with integrated wind measurements from sensors out on the race course," +
                            " the leader board displays information such as in-race ranking, average speeds, distance travelled, ETA" +
                            " (estimated time of arrival at the next mark rounding), gaps to leader, gains and losses per leg.\n\n" +
                            "Check out the results for yourself to see who triumphed - and how they did it."));

            SimplePanel feedbackPanel = new SimplePanel();
            feedbackPanel.addStyleName("feedbackPanel");
            Anchor feedbackLink = new Anchor(stringMessages.feedback(), "mailto:axel.uhl%40sap.com?subject=[SAP Sailing] Feedback");
            feedbackLink.addStyleName("feedbackLink");
            feedbackPanel.add(feedbackLink);

            groupAndFeedbackPanel.add(groupPanel);
            groupAndFeedbackPanel.add(feedbackPanel);

            rootPanel.add(groupAndFeedbackPanel);
        }
        
//        mainPanel.addNorth(logoAndTitlePanel, 68);
//        mainPanel.addEast(feedbackLink, 70);
//        mainPanel.add(panelToDisplay);

        fillEvents();
    }

    @Override
    public void fillEvents() {
        
    }
    
}
