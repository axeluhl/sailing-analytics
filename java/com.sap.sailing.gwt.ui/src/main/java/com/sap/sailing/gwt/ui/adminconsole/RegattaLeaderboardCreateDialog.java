package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RegattaLeaderboardCreateDialog extends RegattaLeaderboardDialog {
    
    public RegattaLeaderboardCreateDialog(Collection<StrippedLeaderboardDTO> existingLeaderboards, Collection<RegattaDTO> existingRegattas, StringMessages stringConstants,
    		Collection<EventDTO> existingEvents, ErrorReporter errorReporter, DialogCallback<LeaderboardDescriptor> callback) {
        super(stringConstants.createRegattaLeaderboard(), new LeaderboardDescriptor(), existingRegattas, stringConstants, existingEvents, errorReporter, new RegattaLeaderboardDialog.LeaderboardParameterValidator(stringConstants, existingLeaderboards), callback);

        nameTextBox = createTextBox(null);
        displayNameTextBox = createTextBox(null);

        regattaListBox = createListBox(false);
        regattaListBox.addItem(stringConstants.pleaseSelectARegatta());
        for (RegattaDTO regatta : existingRegattas) {
            regattaListBox.addItem(regatta.name);
        }
        
        sailingEventsListBox = createSailingEventListBox(this, stringConstants);
        discardThresholdBoxes = initEmptyDiscardThresholdBoxes(this);
    }

}
