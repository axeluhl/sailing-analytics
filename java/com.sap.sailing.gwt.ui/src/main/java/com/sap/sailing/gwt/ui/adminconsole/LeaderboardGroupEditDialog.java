package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

public class LeaderboardGroupEditDialog extends LeaderboardGroupDialog {
    public LeaderboardGroupEditDialog(LeaderboardGroupDTO group, Collection<LeaderboardGroupDTO> otherExistingGroups,
            StringMessages stringMessages, DialogCallback<LeaderboardGroupDTO> callback) {
        super(group, stringMessages, callback, otherExistingGroups);
        // don't allow editing the overall leaderboard's scoring scheme if the group already uses an overall leaderboard
        getOverallLeaderboardScoringSchemeListBox().setEnabled(!group.hasOverallLeaderboard());
        nameEntryField = createTextBox(group.name);
        descriptionEntryField = createTextArea(group.description);
        displayGroupsInReverseOrderCheckBox.setValue(group.displayGroupsInReverseOrder);
        displayGroupsInReverseOrderCheckBox.setEnabled(false);
        useOverallLeaderboardCheckBox.setValue(group.hasOverallLeaderboard());     
    }
}
