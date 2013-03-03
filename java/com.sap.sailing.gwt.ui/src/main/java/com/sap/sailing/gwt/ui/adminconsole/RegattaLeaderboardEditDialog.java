package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RegattaLeaderboardEditDialog extends RegattaLeaderboardDialog {
    
    public RegattaLeaderboardEditDialog(Collection<StrippedLeaderboardDTO> otherExistingLeaderboards, Collection<RegattaDTO> existingRegattas,
            LeaderboardDescriptor leaderboardDescriptor, StringMessages stringConstants, ErrorReporter errorReporter,
            DialogCallback<LeaderboardDescriptor> callback) {
        super(stringConstants.editRegattaLeaderboard(), leaderboardDescriptor, existingRegattas, stringConstants, errorReporter, new RegattaLeaderboardDialog.LeaderboardParameterValidator(
                stringConstants, otherExistingLeaderboards), callback);
        
        nameTextBox = createTextBox(leaderboardDescriptor.getName());
        displayNameTextBox = createTextBox(leaderboardDescriptor.getDisplayName());
        nameTextBox.setEnabled(false);
        nameTextBox.setVisibleLength(50);
        displayNameTextBox.setVisibleLength(50);

        regattaListBox = createSortedRegattaListBox(existingRegattas, leaderboardDescriptor.getRegattaName());
        regattaListBox.setEnabled(false);
        discardThresholdBoxes = initPrefilledDiscardThresholdBoxes(leaderboardDescriptor.getDiscardThresholds(), this);
    }
}
