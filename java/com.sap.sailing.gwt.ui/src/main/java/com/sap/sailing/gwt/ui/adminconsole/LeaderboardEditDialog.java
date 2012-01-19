package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.LongBox;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

public class LeaderboardEditDialog extends LeaderboardDialog {
    
    public LeaderboardEditDialog(Collection<LeaderboardDTO> otherExistingLeaderboards, LeaderboardDTO leaderboard, StringMessages stringConstants,
            ErrorReporter errorReporter, AsyncCallback<LeaderboardDTO> callback) {
        super(leaderboard, stringConstants, errorReporter, new LeaderboardDialog.LeaderboardParameterValidator(
                stringConstants, otherExistingLeaderboards), callback);
        
        entryField = createTextBox(leaderboard.name);

        discardThresholdBoxes = new LongBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < super.discardThresholdBoxes.length; i++) {
            if (i < leaderboard.discardThresholds.length) {
                discardThresholdBoxes[i] = createLongBox(leaderboard.discardThresholds[i], 2);
            } else {
                discardThresholdBoxes[i] = createLongBoxWithOptionalValue(null, 2);
            }
            discardThresholdBoxes[i].setVisibleLength(2);
        }
    }
}
