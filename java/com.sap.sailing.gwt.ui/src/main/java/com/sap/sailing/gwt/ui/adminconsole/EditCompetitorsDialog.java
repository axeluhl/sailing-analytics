package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

public class EditCompetitorsDialog extends DataEntryDialog<List<CompetitorWithBoatDTO>> {
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final String leaderboardName;
    private final String boatClassName;

    protected static class CompetitorsValidator implements Validator<List<CompetitorWithBoatDTO>> {
        public CompetitorsValidator() {
            super();
        }

        @Override
        public String getErrorMessage(List<CompetitorWithBoatDTO> valueToValidate) {
            return null;
        }
    }
        
    public EditCompetitorsDialog(final SailingServiceAsync sailingService, final UserService userService,
            final String leaderboardName, final String boatClassName, final StringMessages stringMessages,
            final ErrorReporter errorReporter, DialogCallback<List<CompetitorWithBoatDTO>> callback) {
        super(stringMessages.actionEditCompetitors(), null, stringMessages.ok(), stringMessages.cancel(),
                new CompetitorsValidator(), callback);
        this.sailingService = sailingService;
        this.userService = userService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.leaderboardName = leaderboardName;
        this.boatClassName = boatClassName;
    }

    @Override
    protected List<CompetitorWithBoatDTO> getResult() {
        return null;
    }

    @Override
    protected Widget getAdditionalWidget() {
        CompetitorPanel competitorPanel = new CompetitorPanel(sailingService, userService, leaderboardName,
                boatClassName, stringMessages, errorReporter);
        return competitorPanel;
    }
}
