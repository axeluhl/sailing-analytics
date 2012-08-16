package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.LongBox;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class FlexibleLeaderboardEditDialog extends FlexibleLeaderboardDialog {
    
    public FlexibleLeaderboardEditDialog(Collection<StrippedLeaderboardDTO> otherExistingLeaderboards,
            LeaderboardDescriptor leaderboard, StringMessages stringConstants, ErrorReporter errorReporter,
            AsyncCallback<LeaderboardDescriptor> callback) {
        super(stringConstants.editFlexibleLeaderboard(), leaderboard, stringConstants, errorReporter, new FlexibleLeaderboardDialog.LeaderboardParameterValidator(
                stringConstants, otherExistingLeaderboards), callback);
        
        nameTextBox = createTextBox(leaderboard.name);

        scoringSchemeListBox = createListBox(false);
        int j = 0;
        for (ScoringSchemeType scoringSchemeType: ScoringSchemeType.values()) {
            scoringSchemeListBox.addItem(ScoringSchemeTypeFormatter.format(scoringSchemeType, stringConstants));
            if(leaderboard.scoringScheme == scoringSchemeType) {
                scoringSchemeListBox.setSelectedIndex(j);
            }
            j++;
        }
        scoringSchemeListBox.setEnabled(false);

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
