package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class FlexibleLeaderboardCreateDialog extends FlexibleLeaderboardDialog {

    public FlexibleLeaderboardCreateDialog(Collection<StrippedLeaderboardDTO> existingLeaderboards, StringMessages stringMessages,
            Collection<EventDTO> existingEvents, ErrorReporter errorReporter, DialogCallback<LeaderboardDescriptor> callback) {
        super(stringMessages.createFlexibleLeaderboard(), new LeaderboardDescriptor(), stringMessages, existingEvents,
                errorReporter, new FlexibleLeaderboardDialog.LeaderboardParameterValidator(stringMessages, existingLeaderboards), callback);
        nameTextBox = createTextBox(null);
        nameTextBox.ensureDebugId("NameTextBox");
        nameTextBox.setVisibleLength(50);
        
        displayNameTextBox = createTextBox(null);
        displayNameTextBox.ensureDebugId("DisplayNameTextBox");
        displayNameTextBox.setVisibleLength(50);

        scoringSchemeListBox = createScoringSchemeListBox(this, stringMessages);
        sailingEventsListBox = createSailingEventListBox();
        discardThresholdBoxes = new DiscardThresholdBoxes(this, stringMessages);
    }
}
