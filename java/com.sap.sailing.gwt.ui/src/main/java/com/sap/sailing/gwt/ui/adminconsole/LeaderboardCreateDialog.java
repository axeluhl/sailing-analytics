package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.LongBox;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class LeaderboardCreateDialog extends LeaderboardDialog {
    
    public LeaderboardCreateDialog(Collection<StrippedLeaderboardDTO> existingLeaderboards, Collection<RegattaDTO> existingRegattas, StringMessages stringConstants,
            ErrorReporter errorReporter, AsyncCallback<StrippedLeaderboardDTO> callback) {
        super(new StrippedLeaderboardDTO(), existingRegattas, stringConstants, errorReporter, new LeaderboardDialog.LeaderboardParameterValidator(stringConstants, existingLeaderboards), callback);

        entryField = createTextBox(null);

        regattaListBox = createListBox(false);
        regattaListBox.setEnabled(true);

        discardThresholdBoxes = new LongBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            discardThresholdBoxes[i] = createLongBoxWithOptionalValue(null, 2);
            discardThresholdBoxes[i].setVisibleLength(2);
        }
    }

}
