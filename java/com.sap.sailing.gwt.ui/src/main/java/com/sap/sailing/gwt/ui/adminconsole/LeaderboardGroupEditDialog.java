package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

public class LeaderboardGroupEditDialog extends LeaderboardGroupDialog {

    public LeaderboardGroupEditDialog(LeaderboardGroupDTO group, Collection<LeaderboardGroupDTO> otherExistingGroups,
            StringMessages stringConstants, AsyncCallback<LeaderboardGroupDTO> callback) {
        super(group, new LeaderboardGroupParameterValidator(stringConstants, otherExistingGroups), stringConstants, callback);

        nameEntryField = createTextBox(group.name);
        descriptionEntryField = createTextArea(group.description);
    }

}
