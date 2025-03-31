package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.Refresher;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

public abstract class AbstractCompetitorRegistrationDialog extends DataEntryDialog<Set<CompetitorDTO>> {
    protected final ErrorReporter errorReporter;
    protected final SailingServiceWriteAsync sailingService;
    protected final StringMessages stringMessages;
    protected final String leaderboardName;
    protected final CompetitorRegistrationsPanel competitorRegistrationsPanel;

    public AbstractCompetitorRegistrationDialog(SailingServiceWriteAsync sailingServiceWrite,
            final UserService userService, Refresher<CompetitorDTO> competitorsRefresher,
            Refresher<BoatDTO> boatsRefresher, StringMessages stringMessages, ErrorReporter errorReporter,
            boolean editable, com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Set<CompetitorDTO>> callback,
            String leaderboardName, boolean canBoatsOfCompetitorsChangePerRace, String boatClass,
            String okButtonMessage, Validator<Set<CompetitorDTO>> validator,
            Consumer<Pair<CompetitorRegistrationsPanel, AsyncCallback<Collection<CompetitorDTO>>>> registeredCompetitorsRetriever) {
        super(stringMessages.registerCompetitors(), /* messsage */null, okButtonMessage, stringMessages.cancel(),
                validator, callback);
        this.ensureDebugId("registerCompetitorsDialog");
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.sailingService = sailingServiceWrite;
        this.leaderboardName = leaderboardName;
        this.competitorRegistrationsPanel = new CompetitorRegistrationsPanel(sailingServiceWrite, userService,
                competitorsRefresher, boatsRefresher, stringMessages, errorReporter, editable, leaderboardName,
                canBoatsOfCompetitorsChangePerRace, boatClass, () -> validateAndUpdate(),
                registeredCompetitorsRetriever, /* restrictPoolToLeaderboard */ false,
                getAdditionalWidgetsToInsertAboveCompetitorTables(stringMessages));
    }

    @Override
    protected Widget getAdditionalWidget() {
        return competitorRegistrationsPanel;
    }

    @Override
    protected Set<CompetitorDTO> getResult() {
        return competitorRegistrationsPanel.getResult();
    }

    protected abstract Widget[] getAdditionalWidgetsToInsertAboveCompetitorTables(StringMessages stringMessages);
}
