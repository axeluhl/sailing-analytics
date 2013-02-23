package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RegattaLeaderboardEditDialog extends RegattaLeaderboardDialog {
    
    public RegattaLeaderboardEditDialog(Collection<StrippedLeaderboardDTO> otherExistingLeaderboards, Collection<RegattaDTO> existingRegattas,
            LeaderboardDescriptor leaderboard, StringMessages stringConstants, ErrorReporter errorReporter,
            DialogCallback<LeaderboardDescriptor> callback) {
        super(stringConstants.editRegattaLeaderboard(), leaderboard, existingRegattas, stringConstants, errorReporter, new RegattaLeaderboardDialog.LeaderboardParameterValidator(
                stringConstants, otherExistingLeaderboards), callback);
        
        nameTextBox = createTextBox(leaderboard.getName());
        displayNameTextBox = createTextBox(leaderboard.getDisplayName());

        regattaListBox = createListBox(false);
        regattaListBox.addItem(stringConstants.pleaseSelectARegatta());
        int i=1;
        for (RegattaDTO regatta : existingRegattas) {
            regattaListBox.addItem(regatta.name);
            if (regatta.name.equals(leaderboard.getRegattaName())) {
                regattaListBox.setSelectedIndex(i);
            }
            i++;
        }
        regattaListBox.setEnabled(false);
        //TODO Preselect selected event and course area
        discardThresholdBoxes = initPrefilledDiscardThresholdBoxes(leaderboard.getDiscardThresholds(), this);
    }
}
