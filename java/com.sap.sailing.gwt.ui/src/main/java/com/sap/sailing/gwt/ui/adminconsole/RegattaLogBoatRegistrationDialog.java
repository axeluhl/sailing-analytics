package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegattaLogBoatRegistrationDialog extends DataEntryDialog<Set<BoatDTO>> {
    protected final ErrorReporter errorReporter;
    protected final SailingServiceAsync sailingService;
    protected final StringMessages stringMessages;
    protected final String leaderboardName;
    protected final boolean canBoatsOfCompetitorsChangePerRace;
    protected final BoatRegistrationsPanel boatRegistrationsPanel;

    public RegattaLogBoatRegistrationDialog(String boatClass, SailingServiceAsync sailingService,
            StringMessages stringMessages, ErrorReporter errorReporter, boolean editable, String leaderboardName, boolean canBoatsOfCompetitorsChangePerRace,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Set<BoatDTO>> callback) {
        super(stringMessages.registerBoats(), /* messsage */null, stringMessages.save(), stringMessages.cancel(),
                /* validator */ null, callback);
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.leaderboardName = leaderboardName;
        this.canBoatsOfCompetitorsChangePerRace = canBoatsOfCompetitorsChangePerRace;
        this.boatRegistrationsPanel = new BoatRegistrationsPanel(sailingService, stringMessages, errorReporter, editable, leaderboardName,
                canBoatsOfCompetitorsChangePerRace, boatClass, ()->validateAndUpdate(), getRegisteredBoatsRetriever(), /* restrictPoolToLeaderboard */ false);
    }

    protected Consumer<AsyncCallback<Collection<BoatDTO>>> getRegisteredBoatsRetriever() {
        return (callback)->getRegisteredBoats(callback);
    }

    private void getRegisteredBoats(AsyncCallback<Collection<BoatDTO>> callback) {
        if (boatRegistrationsPanel.showOnlyBoatsOfLog()) {
            sailingService.getBoatRegistrationsInRegattaLog(leaderboardName, callback);
        } else {
            sailingService.getBoatRegistrationsForLeaderboard(leaderboardName, callback);
        }
    }

    @Override
    protected Widget getAdditionalWidget() {
        return boatRegistrationsPanel;
    }

    @Override
    protected Set<BoatDTO> getResult() {
        return boatRegistrationsPanel.getResult();
    }

}
