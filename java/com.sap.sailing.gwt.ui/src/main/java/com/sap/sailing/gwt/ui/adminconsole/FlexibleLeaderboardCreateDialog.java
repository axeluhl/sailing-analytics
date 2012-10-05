package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class FlexibleLeaderboardCreateDialog extends FlexibleLeaderboardDialog {
    
    public FlexibleLeaderboardCreateDialog(Collection<StrippedLeaderboardDTO> existingLeaderboards, StringMessages stringMessages,
            ErrorReporter errorReporter, DialogCallback<LeaderboardDescriptor> callback) {
        super(stringMessages.createFlexibleLeaderboard(), new LeaderboardDescriptor(), stringMessages, errorReporter, new FlexibleLeaderboardDialog.LeaderboardParameterValidator(stringMessages, existingLeaderboards), callback);
        nameTextBox = createTextBox(null);
        scoringSchemeListBox = createScoringSchemeListBox(this, stringMessages);
        discardThresholdBoxes = initEmptyDiscardThresholdBoxes(this);
    }
}
