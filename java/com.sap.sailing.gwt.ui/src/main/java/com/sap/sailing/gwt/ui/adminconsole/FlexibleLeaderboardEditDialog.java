package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class FlexibleLeaderboardEditDialog extends FlexibleLeaderboardDialog {
    
    public FlexibleLeaderboardEditDialog(Collection<StrippedLeaderboardDTO> otherExistingLeaderboards,
            LeaderboardDescriptor leaderboard, StringMessages stringConstants, ErrorReporter errorReporter,
            DialogCallback<LeaderboardDescriptor> callback) {
        super(stringConstants.editFlexibleLeaderboard(), leaderboard, stringConstants, errorReporter, new FlexibleLeaderboardDialog.LeaderboardParameterValidator(
                stringConstants, otherExistingLeaderboards), callback);
        
        nameTextBox = createTextBox(leaderboard.getName());
        displayNameTextBox = createTextBox(leaderboard.getDisplayName()); 
        scoringSchemeListBox = createListBox(false);
        int j = 0;
        for (ScoringSchemeType scoringSchemeType: ScoringSchemeType.values()) {
            scoringSchemeListBox.addItem(ScoringSchemeTypeFormatter.format(scoringSchemeType, stringConstants));
            if(leaderboard.getScoringScheme() == scoringSchemeType) {
                scoringSchemeListBox.setSelectedIndex(j);
            }
            j++;
        }
        scoringSchemeListBox.setEnabled(false);
        discardThresholdBoxes = initPrefilledDiscardThresholdBoxes(leaderboard.getDiscardThresholds(), this);
    }
}
