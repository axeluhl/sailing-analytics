package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.Refresher;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

public class EditCompetitorsDialog extends DataEntryDialog<List<CompetitorWithBoatDTO>> {
    private final SailingServiceWriteAsync sailingServiceWrite;
    private final UserService userService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final String leaderboardName;
    private final String boatClassName;
    private final boolean createWithBoatByDefault;
    private final Refresher<CompetitorDTO> competitorsRefresher;

    protected static class CompetitorsValidator implements Validator<List<CompetitorWithBoatDTO>> {
        public CompetitorsValidator() {
            super();
        }

        @Override
        public String getErrorMessage(List<CompetitorWithBoatDTO> valueToValidate) {
            return null;
        }
    }
        
    public EditCompetitorsDialog(final SailingServiceWriteAsync sailingServiceWrite, final UserService userService,
            Refresher<CompetitorDTO> competitorsRefresher, final String leaderboardName, final String boatClassName,
            boolean createWithBoatByDefault, final StringMessages stringMessages, final ErrorReporter errorReporter, DialogCallback<List<CompetitorWithBoatDTO>> callback) {
        super(stringMessages.actionEditCompetitors(), null, stringMessages.ok(), stringMessages.cancel(),
                new CompetitorsValidator(), callback);
        this.sailingServiceWrite = sailingServiceWrite;
        this.userService = userService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.leaderboardName = leaderboardName;
        this.boatClassName = boatClassName;
        this.createWithBoatByDefault = createWithBoatByDefault;
        this.competitorsRefresher = competitorsRefresher;
    }

    @Override
    protected List<CompetitorWithBoatDTO> getResult() {
        return null;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final CompetitorPanel competitorPanel = new CompetitorPanel(sailingServiceWrite, userService, competitorsRefresher,
                leaderboardName, boatClassName, createWithBoatByDefault, stringMessages, errorReporter);
        return competitorPanel;
    }
}
