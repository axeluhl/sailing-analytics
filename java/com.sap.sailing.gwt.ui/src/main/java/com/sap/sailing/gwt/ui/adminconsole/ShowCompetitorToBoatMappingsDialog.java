package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class ShowCompetitorToBoatMappingsDialog extends DataEntryDialog<List<CompetitorDTO>> {
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final String leaderboardName;
    private final String raceColumnName;
    private final String fleetName;
    private final boolean enableActions;

    protected static class CompetitorsValidator implements Validator<List<CompetitorDTO>> {
        public CompetitorsValidator() {
            super();
        }

        @Override
        public String getErrorMessage(List<CompetitorDTO> valueToValidate) {
            return null;
        }
    }
        
    public ShowCompetitorToBoatMappingsDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages, 
            final ErrorReporter errorReporter, String leaderboardName, final String raceColumnName, final String fleetName,
            String raceName, boolean enableActions, DialogCallback<List<CompetitorDTO>> callback) {
        super(stringMessages.actionShowCompetitorToBoatAssignments(),
                stringMessages.race() + ":" + raceName, stringMessages.ok(), stringMessages.cancel(), new CompetitorsValidator(), callback);
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.enableActions = enableActions;
    }

    @Override
    protected List<CompetitorDTO> getResult() {
        return null;
    }

    @Override
    protected Widget getAdditionalWidget() {
        CompetitorToBoatMappingsPanel competitorPanel = new CompetitorToBoatMappingsPanel(sailingService,
                stringMessages, errorReporter, leaderboardName, raceColumnName, fleetName, enableActions);

        return competitorPanel; 
    }
}
