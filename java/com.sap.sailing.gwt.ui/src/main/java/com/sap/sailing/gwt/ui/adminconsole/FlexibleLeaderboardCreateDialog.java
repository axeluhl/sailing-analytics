package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.LongBox;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class FlexibleLeaderboardCreateDialog extends FlexibleLeaderboardDialog {
    
    public FlexibleLeaderboardCreateDialog(Collection<StrippedLeaderboardDTO> existingLeaderboards, StringMessages stringConstants,
            ErrorReporter errorReporter, AsyncCallback<LeaderboardDescriptor> callback) {
        super(stringConstants.createFlexibleLeaderboard(), new LeaderboardDescriptor(), stringConstants, errorReporter, new FlexibleLeaderboardDialog.LeaderboardParameterValidator(stringConstants, existingLeaderboards), callback);

        nameTextBox = createTextBox(null);

        scoringSchemeListBox = createListBox(false);
        for (ScoringSchemeType scoringSchemeType: ScoringSchemeType.values()) {
            scoringSchemeListBox.addItem(ScoringSchemeTypeFormatter.format(scoringSchemeType, stringConstants));
        }

        discardThresholdBoxes = new LongBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            discardThresholdBoxes[i] = createLongBoxWithOptionalValue(null, 2);
            discardThresholdBoxes[i].setVisibleLength(2);
        }
    }

}
