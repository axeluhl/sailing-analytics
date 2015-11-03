package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class RaceLogCompetitorRegistrationDialog extends AbstractCompetitorRegistrationsDialog {

    private String leaderboardName;
    private String fleetName;
    private String raceColumnName;

    public RaceLogCompetitorRegistrationDialog(String boatClass, SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean editable, String leaderboardName, String raceColumnName, String fleetName,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Set<CompetitorDTO>> callback) {
        super(sailingService, stringMessages, errorReporter, editable, callback, boatClass);
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
    }

    @Override
    protected void setRegisteredCompetitors() {
        sailingService.getCompetitorRegistrationsOnRaceLog(leaderboardName, raceColumnName, fleetName,
                new AsyncCallback<Collection<CompetitorDTO>>() {
                    @Override
                    public void onSuccess(Collection<CompetitorDTO> registeredCompetitors) {
                        move(allCompetitorsTable, registeredCompetitorsTable, registeredCompetitors);
                    }

                    @Override
                    public void onFailure(Throwable reason) {
                        errorReporter.reportError("Could not load already registered competitors: "
                                + reason.getMessage());
                    }
                });
    }

}
