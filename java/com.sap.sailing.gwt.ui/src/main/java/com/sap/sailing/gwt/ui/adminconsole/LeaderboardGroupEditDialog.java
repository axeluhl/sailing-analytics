package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

public class LeaderboardGroupEditDialog extends LeaderboardGroupDialog {
    public LeaderboardGroupEditDialog(LeaderboardGroupDTO group, Collection<LeaderboardGroupDTO> otherExistingGroups,
            StringMessages stringMessages, DialogCallback<LeaderboardGroupDescriptor> callback) {
        super(group, stringMessages, callback, otherExistingGroups);
        // don't allow editing the overall leaderboard's scoring scheme if the group already uses an overall leaderboard
        getOverallLeaderboardScoringSchemeListBox().setEnabled(!group.hasOverallLeaderboard());
        nameEntryField = createTextBox(group.getName(), 50);
        descriptionEntryField = createTextArea(group.description);
        displayNameEntryField = createTextBox(group.getDisplayName());
        displayLeaderboardsInReverseOrderCheckBox.setValue(group.displayLeaderboardsInReverseOrder);
        displayLeaderboardsInReverseOrderCheckBox.setEnabled(false);
        useOverallLeaderboardCheckBox.setValue(group.hasOverallLeaderboard());
    }
}
