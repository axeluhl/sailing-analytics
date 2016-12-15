package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class RaceImportCompetitorCallback extends ImportCompetitorCallback {
    private final String leaderboardName;
    private final String raceColumnName;
    private final String fleetName;

    public RaceImportCompetitorCallback(String leaderboardName, String raceColumnName, String fleetName,
            SailingServiceAsync sailingService, ErrorReporter errorReporter, StringMessages stringMessages) {
        super(sailingService, errorReporter, stringMessages);
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
    }

    @Override
    protected void registerCompetitors(Set<CompetitorDTO> competitors) {
        sailingService.setCompetitorRegistrationsInRaceLog(leaderboardName, raceColumnName, fleetName, competitors,
                new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.couldNotSaveCompetitorRegistrations(caught.getMessage()));
                    }
                });
    }
}
