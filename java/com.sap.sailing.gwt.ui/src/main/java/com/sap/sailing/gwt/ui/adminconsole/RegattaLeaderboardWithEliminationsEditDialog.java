package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RegattaLeaderboardWithEliminationsEditDialog extends RegattaLeaderboardWithEliminationsDialog {
    public RegattaLeaderboardWithEliminationsEditDialog(Collection<StrippedLeaderboardDTO> otherExistingLeaderboards,
            Collection<RegattaDTO> existingRegattas, LeaderboardDescriptor leaderboardDescriptor,
            StringMessages stringMessages, ErrorReporter errorReporter, DialogCallback<LeaderboardDescriptor> callback) {
        super(stringMessages.editRegattaLeaderboard(), leaderboardDescriptor, existingRegattas,
                otherExistingLeaderboards, stringMessages, errorReporter,
                new RegattaLeaderboardWithEliminationsDialog.LeaderboardParameterValidator(stringMessages,
                        otherExistingLeaderboards),
                callback);
        nameTextBox.setEnabled(false);
        nameTextBox.setText(leaderboardDescriptor.getName());
        for (int i=0; i<regattaLeaderboardsListBox.getItemCount(); i++) {
            if (regattaLeaderboardsListBox.getValue(i).equals(leaderboardDescriptor.getRegattaName())) {
                regattaLeaderboardsListBox.setSelectedIndex(i);
                break;
            }
        }
        regattaLeaderboardsListBox.setEnabled(false);
        displayNameTextBox.setText(leaderboardDescriptor.getDisplayName());
    }
}
