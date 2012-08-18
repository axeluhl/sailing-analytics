package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class FlexibleLeaderboardCreateDialog extends FlexibleLeaderboardDialog {
    
    public FlexibleLeaderboardCreateDialog(Collection<StrippedLeaderboardDTO> existingLeaderboards, StringMessages stringMessages,
            ErrorReporter errorReporter, AsyncCallback<LeaderboardDescriptor> callback) {
        super(stringMessages.createFlexibleLeaderboard(), new LeaderboardDescriptor(), stringMessages, errorReporter, new FlexibleLeaderboardDialog.LeaderboardParameterValidator(stringMessages, existingLeaderboards), callback);
        nameTextBox = createTextBox(null);
        scoringSchemeListBox = createScoringSchemeListBox(this, stringMessages);
        discardThresholdBoxes = initEmptyDiscardThresholdBoxes(this);
    }
}
