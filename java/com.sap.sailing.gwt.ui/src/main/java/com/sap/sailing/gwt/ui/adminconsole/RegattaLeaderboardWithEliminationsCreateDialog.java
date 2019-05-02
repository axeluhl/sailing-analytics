package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Consumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

public class RegattaLeaderboardWithEliminationsCreateDialog extends RegattaLeaderboardWithEliminationsDialog {

    public RegattaLeaderboardWithEliminationsCreateDialog(SailingServiceAsync sailingService, final UserService userService,
            Collection<StrippedLeaderboardDTO> existingLeaderboards, Collection<RegattaDTO> existingRegattas, StringMessages stringMessages,
            ErrorReporter errorReporter, DialogCallback<LeaderboardDescriptorWithEliminations> callback) {
        super(sailingService, userService, stringMessages.createRegattaLeaderboard(), new LeaderboardDescriptorWithEliminations(
                new LeaderboardDescriptor(), Collections.emptySet()), existingRegattas,
                existingLeaderboards, stringMessages,
                errorReporter, new RegattaLeaderboardWithEliminationsDialog.LeaderboardParameterValidator(stringMessages, existingLeaderboards), callback);
    }

    @Override
    protected Consumer<AsyncCallback<Collection<CompetitorDTO>>> getEliminatedCompetitorsRetriever() {
        eliminatedCompetitors = new HashSet<>();
        return callback->callback.onSuccess(eliminatedCompetitors);
    }
}
