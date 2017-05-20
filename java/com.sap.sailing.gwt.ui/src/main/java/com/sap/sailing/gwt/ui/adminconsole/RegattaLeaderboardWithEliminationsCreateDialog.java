package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RegattaLeaderboardWithEliminationsCreateDialog extends RegattaLeaderboardWithEliminationsDialog {

    public RegattaLeaderboardWithEliminationsCreateDialog(Collection<StrippedLeaderboardDTO> existingLeaderboards,
            Collection<RegattaDTO> existingRegattas, StringMessages stringMessages, ErrorReporter errorReporter,
            DialogCallback<LeaderboardDescriptor> callback) {
        super(stringMessages.createRegattaLeaderboard(), new LeaderboardDescriptor(), existingRegattas, existingLeaderboards,
                stringMessages, errorReporter,
                new RegattaLeaderboardWithEliminationsDialog.LeaderboardParameterValidator(stringMessages, existingLeaderboards), callback);
    }
}
