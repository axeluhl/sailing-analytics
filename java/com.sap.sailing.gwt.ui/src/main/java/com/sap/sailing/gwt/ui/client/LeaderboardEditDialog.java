package com.sap.sailing.gwt.ui.client;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IntegerBox;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;

public class LeaderboardEditDialog extends LeaderboardDialog {
    
    public LeaderboardEditDialog(Collection<LeaderboardDAO> otherExistingLeaderboards, LeaderboardDAO leaderboard, StringConstants stringConstants,
            ErrorReporter errorReporter, AsyncCallback<LeaderboardDAO> callback) {
        super(leaderboard, stringConstants, errorReporter, new LeaderboardDialog.LeaderboardParameterValidator(
                stringConstants, otherExistingLeaderboards), callback);
        
        entryField = createTextBox(leaderboard.name);

        discardThresholdBoxes = new IntegerBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < super.discardThresholdBoxes.length; i++) {
            if (i < leaderboard.discardThresholds.length) {
                discardThresholdBoxes[i] = createIntegerBox(leaderboard.discardThresholds[i], 2);
            } else {
                discardThresholdBoxes[i] = createIntegerBoxWithOptionalValue(null, 2);
            }
            discardThresholdBoxes[i].setVisibleLength(2);
        }
    }
}
