package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.LongBox;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringConstants;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;

public class LeaderboardCreateDialog extends LeaderboardDialog{
    
    public LeaderboardCreateDialog(Collection<LeaderboardDAO> existingLeaderboards, StringConstants stringConstants,
            ErrorReporter errorReporter, AsyncCallback<LeaderboardDAO> callback) {
        super(new LeaderboardDAO(), stringConstants, errorReporter, new LeaderboardDialog.LeaderboardParameterValidator(stringConstants, existingLeaderboards), callback);

        entryField = createTextBox(null);

        discardThresholdBoxes = new LongBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            discardThresholdBoxes[i] = createIntegerBoxWithOptionalValue(null, 2);
            discardThresholdBoxes[i].setVisibleLength(2);
        }
    }

}
