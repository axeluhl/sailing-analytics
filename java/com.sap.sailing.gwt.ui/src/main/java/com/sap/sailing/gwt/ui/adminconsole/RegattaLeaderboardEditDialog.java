package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RegattaLeaderboardEditDialog extends RegattaLeaderboardDialog {
    
    public RegattaLeaderboardEditDialog(Collection<StrippedLeaderboardDTO> otherExistingLeaderboards, Collection<RegattaDTO> existingRegattas,
            LeaderboardDescriptor leaderboard, StringMessages stringConstants, ErrorReporter errorReporter,
            AsyncCallback<LeaderboardDescriptor> callback) {
        super(stringConstants.editRegattaLeaderboard(), leaderboard, existingRegattas, stringConstants, errorReporter, new RegattaLeaderboardDialog.LeaderboardParameterValidator(
                stringConstants, otherExistingLeaderboards), callback);
        
        nameTextBox = createTextBox(leaderboard.name);

        regattaListBox = createListBox(false);
        regattaListBox.addItem(stringConstants.pleaseSelectARegatta());
        for (RegattaDTO regatta : existingRegattas) {
            regattaListBox.addItem(regatta.name);
        }
        regattaListBox.setEnabled(false);
        discardThresholdBoxes = initPrefilledDiscardThresholdBoxes(leaderboard);
    }
}
