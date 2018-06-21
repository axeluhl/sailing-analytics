package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class RegattaLogCompetitorRegistrationDialog extends AbstractCompetitorRegistrationDialog {

    public RegattaLogCompetitorRegistrationDialog(String boatClass, SailingServiceAsync sailingService,
            StringMessages stringMessages, ErrorReporter errorReporter, boolean editable, String leaderboardName, boolean canBoatsOfCompetitorsChangePerRace,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Set<CompetitorDTO>> callback) {
        this(boatClass, sailingService, stringMessages, errorReporter, editable, leaderboardName,
                canBoatsOfCompetitorsChangePerRace, /* validator */ null, callback);
    }
    
    public RegattaLogCompetitorRegistrationDialog(String boatClass, SailingServiceAsync sailingService,
            StringMessages stringMessages, ErrorReporter errorReporter, boolean editable, String leaderboardName,
            boolean canBoatsOfCompetitorsChangePerRace, Validator<Set<CompetitorDTO>> validator,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Set<CompetitorDTO>> callback) {
        super(sailingService, stringMessages, errorReporter, editable, callback, leaderboardName,
                canBoatsOfCompetitorsChangePerRace, boatClass, stringMessages.save(), validator);
    }

    @Override
    protected Consumer<AsyncCallback<Collection<CompetitorDTO>>> getRegisteredCompetitorsRetriever() {
        return (callback)->getRegisteredCompetitors(callback);
    }

    private void getRegisteredCompetitors(AsyncCallback<Collection<CompetitorDTO>> callback) {
        if (competitorRegistrationsPanel.showOnlyCompetitorsOfLog()) {
            sailingService.getCompetitorRegistrationsInRegattaLog(leaderboardName, callback);
        } else {
            sailingService.getCompetitorRegistrationsForLeaderboard(leaderboardName, callback);
        }
    }

    @Override
    protected Widget[] getAdditionalWidgetsToInsertAboveCompetitorTables(StringMessages stringMessages) {
        return null;
    }
}
