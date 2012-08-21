package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

public class LeaderboardGroupEditDialog extends LeaderboardGroupDialog {
    public LeaderboardGroupEditDialog(LeaderboardGroupDTO group, Collection<LeaderboardGroupDTO> otherExistingGroups,
            StringMessages stringMessages, AsyncCallback<LeaderboardGroupDTO> callback) {
        super(group, stringMessages, callback, otherExistingGroups);
        // don't allow editing the overall leaderboard's scoring scheme if the group already uses an overall leaderboard
        getOverallLeaderboardScoringSchemeListBox().setEnabled(!group.hasOverallLeaderboard());
        nameEntryField = createTextBox(group.name);
        descriptionEntryField = createTextArea(group.description);
    }

    @Override
    protected Widget getAdditionalWidget() {
        Widget result = super.getAdditionalWidget();
        getUseOverallLeaderboardCheckBox().setValue(group.hasOverallLeaderboard(), /* fire event */ true);
        return result;
    }
}
