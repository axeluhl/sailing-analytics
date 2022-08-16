package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.Refresher;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

public class RegattaLogCompetitorRegistrationDialog extends AbstractCompetitorRegistrationDialog {

    public RegattaLogCompetitorRegistrationDialog(String boatClass, SailingServiceWriteAsync sailingServiceWrite,
            final UserService userService, Refresher<CompetitorDTO> competitorsRefresher,
            Refresher<BoatDTO> boatsRefresher, StringMessages stringMessages, ErrorReporter errorReporter,
            boolean editable, String leaderboardName, boolean canBoatsOfCompetitorsChangePerRace,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Set<CompetitorDTO>> callback) {
        this(boatClass, sailingServiceWrite, userService, competitorsRefresher, boatsRefresher, stringMessages,
                errorReporter, editable, leaderboardName, canBoatsOfCompetitorsChangePerRace, /* validator */ null,
                callback);
    }

    public RegattaLogCompetitorRegistrationDialog(String boatClass, SailingServiceWriteAsync sailingServiceWrite,
            final UserService userService, Refresher<CompetitorDTO> competitorsRefresher,
            Refresher<BoatDTO> boatsRefresher, StringMessages stringMessages, ErrorReporter errorReporter,
            boolean editable, String leaderboardName, boolean canBoatsOfCompetitorsChangePerRace,
            Validator<Set<CompetitorDTO>> validator,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Set<CompetitorDTO>> callback) {
        super(sailingServiceWrite, userService, competitorsRefresher, boatsRefresher, stringMessages, errorReporter,
                editable, callback, leaderboardName, canBoatsOfCompetitorsChangePerRace, boatClass,
                stringMessages.save(), validator,
                cb -> getRegisteredCompetitors(sailingServiceWrite, leaderboardName, cb));
    }

    private static void getRegisteredCompetitors(SailingServiceWriteAsync sailingService, String leaderboardName,
            Pair<CompetitorRegistrationsPanel, AsyncCallback<Collection<CompetitorDTO>>> callback) {
        if (callback.getA().showOnlyCompetitorsOfLog()) {
            sailingService.getCompetitorRegistrationsInRegattaLog(leaderboardName, callback.getB());
        } else {
            sailingService.getCompetitorRegistrationsForLeaderboard(leaderboardName, callback.getB());
        }
    }

    @Override
    protected Widget[] getAdditionalWidgetsToInsertAboveCompetitorTables(StringMessages stringMessages) {
        return null;
    }
}
