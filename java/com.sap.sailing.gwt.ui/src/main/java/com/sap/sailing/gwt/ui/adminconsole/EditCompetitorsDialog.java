package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class EditCompetitorsDialog extends DataEntryDialog<List<CompetitorDTO>> {
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final String leaderboardName;

    protected static class CompetitorsValidator implements Validator<List<CompetitorDTO>> {
        public CompetitorsValidator() {
            super();
        }

        @Override
        public String getErrorMessage(List<CompetitorDTO> valueToValidate) {
            return null;
        }
    }
        
    public EditCompetitorsDialog(final SailingServiceAsync sailingService, String leaderboardName, final StringMessages stringMessages,
            final ErrorReporter errorReporter, DialogCallback<List<CompetitorDTO>> callback) {
        super(stringMessages.actionEditCompetitors(), null, stringMessages.ok(), stringMessages.cancel(), new CompetitorsValidator(), callback);
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.leaderboardName = leaderboardName;
    }

    @Override
    protected List<CompetitorDTO> getResult() {
        return null;
    }

    @Override
    protected Widget getAdditionalWidget() {
        CompetitorPanel competitorPanel = new CompetitorPanel(sailingService, leaderboardName, stringMessages, errorReporter);

        return competitorPanel; 
    }
}
