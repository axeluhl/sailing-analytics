package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

public class LeaderboardGroupCreateDialog extends LeaderboardGroupDialog {

    public LeaderboardGroupCreateDialog(Collection<LeaderboardGroupDTO> existingGroups,
            StringMessages stringMessages, AsyncCallback<LeaderboardGroupDTO> callback) {
        super(new LeaderboardGroupDTO(), stringMessages,
                callback, existingGroups);
        nameEntryField = createTextBox(null);
        descriptionEntryField = createTextArea(null);
    }

}
