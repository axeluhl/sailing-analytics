package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.LongBox;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RegattaLeaderboardEditDialog extends RegattaLeaderboardDialog {
    
    public RegattaLeaderboardEditDialog(Collection<StrippedLeaderboardDTO> otherExistingLeaderboards, Collection<RegattaDTO> existingRegattas,
            LeaderboardDescriptor leaderboard, StringMessages stringConstants, ErrorReporter errorReporter,
            AsyncCallback<LeaderboardDescriptor> callback) {
        super(leaderboard, existingRegattas, stringConstants, errorReporter, new RegattaLeaderboardDialog.LeaderboardParameterValidator(
                stringConstants, otherExistingLeaderboards), callback);
        
        nameTextBox = createTextBox(leaderboard.name);

        regattaListBox = createListBox(false);
        regattaListBox.addItem(stringConstants.pleaseSelectARegatta());
        for (RegattaDTO regatta : existingRegattas) {
            regattaListBox.addItem(regatta.name);
        }
        regattaListBox.setEnabled(false);

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
