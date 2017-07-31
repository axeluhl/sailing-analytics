package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class OverallLeaderboardSettingsDialogComponent extends MultiRaceLeaderboardSettingsDialogComponent {

    public OverallLeaderboardSettingsDialogComponent(MultiRaceLeaderboardSettings initialSettings, List<String> allRaceColumnNames, StringMessages stringMessages) {
        super(initialSettings, allRaceColumnNames, stringMessages);
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        Widget additionalWidget = super.getAdditionalWidget(dialog);
        additionalWidget.ensureDebugId("OverallLeaderboardSettingsPanel");
        return additionalWidget;
    }
    
}
