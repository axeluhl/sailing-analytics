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
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

public abstract class AbstractCompetitorRegistrationDialog extends DataEntryDialog<Set<CompetitorDTO>> {
    protected final ErrorReporter errorReporter;
    protected final SailingServiceAsync sailingService;
    protected final StringMessages stringMessages;
    protected final String leaderboardName;
    protected final boolean canBoatsOfCompetitorsChangePerRace;
    protected final CompetitorRegistrationsPanel competitorRegistrationsPanel;

    public AbstractCompetitorRegistrationDialog(SailingServiceAsync sailingService, final UserService userService, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean editable,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Set<CompetitorDTO>> callback,
            String leaderboardName, boolean canBoatsOfCompetitorsChangePerRace, String boatClass, String okButtonMessage,
            Validator<Set<CompetitorDTO>> validator) {
        super(stringMessages.registerCompetitors(), /* messsage */null, okButtonMessage, stringMessages.cancel(),
                validator, callback);
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.leaderboardName = leaderboardName;
        this.canBoatsOfCompetitorsChangePerRace = canBoatsOfCompetitorsChangePerRace;
        this.competitorRegistrationsPanel = new CompetitorRegistrationsPanel(sailingService, userService, stringMessages, errorReporter, editable, leaderboardName, canBoatsOfCompetitorsChangePerRace, boatClass,
                ()->validateAndUpdate(), getRegisteredCompetitorsRetriever(), /* restrictPoolToLeaderboard */ false, getAdditionalWidgetsToInsertAboveCompetitorTables(stringMessages));
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        return competitorRegistrationsPanel;
    }

    @Override
    protected Set<CompetitorDTO> getResult() {
        return competitorRegistrationsPanel.getResult();
    }

    protected abstract Consumer<AsyncCallback<Collection<CompetitorDTO>>> getRegisteredCompetitorsRetriever();
    
    protected abstract Widget[] getAdditionalWidgetsToInsertAboveCompetitorTables(StringMessages stringMessages);
}
