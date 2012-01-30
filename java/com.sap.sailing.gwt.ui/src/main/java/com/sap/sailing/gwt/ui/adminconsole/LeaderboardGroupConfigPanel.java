package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.AbstractEventPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class LeaderboardGroupConfigPanel extends AbstractEventPanel {

    public LeaderboardGroupConfigPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, StringMessages stringConstants) {
        super(sailingService, eventRefresher, errorReporter, stringConstants);
        
        VerticalPanel mainPanel = new VerticalPanel();
        add(mainPanel);
        
        //Create leaderboard group GUI
        CaptionPanel leaderboardGroupsCaptionPanel = new CaptionPanel(stringConstants.leaderboardGroups());
    }

    @Override
    public void fillEvents(List<EventDTO> result) {
    }
    
}
