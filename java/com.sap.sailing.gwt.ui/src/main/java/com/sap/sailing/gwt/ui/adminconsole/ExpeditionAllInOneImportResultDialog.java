package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;
import java.util.UUID;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.race.TrackedRaceCreationResultDialog;
import com.sap.sse.common.Util.Pair;

public class ExpeditionAllInOneImportResultDialog extends TrackedRaceCreationResultDialog {

    public ExpeditionAllInOneImportResultDialog(UUID eventId, String regattaName, List<Pair<String, String>> raceEntries,
            String leaderboardName, String leaderboardGroupName) {
        super(StringMessages.INSTANCE.importFinished(), StringMessages.INSTANCE.importFinishedMessage(), eventId,
                regattaName, raceEntries, leaderboardName, leaderboardGroupName);
    }
}