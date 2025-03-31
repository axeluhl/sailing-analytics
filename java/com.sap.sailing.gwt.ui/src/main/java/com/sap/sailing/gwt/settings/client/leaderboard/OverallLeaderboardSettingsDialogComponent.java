package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class OverallLeaderboardSettingsDialogComponent extends MultiRaceLeaderboardSettingsDialogComponent {

    public OverallLeaderboardSettingsDialogComponent(MultiRaceLeaderboardSettings initialSettings,
            List<String> allRaceColumnNames, StringMessages stringMessages, Iterable<DetailType> availableDetailTypes,
            PaywallResolver paywallResolver, AbstractLeaderboardDTO leaderboardDTO) {
        super(initialSettings, allRaceColumnNames, stringMessages, availableDetailTypes,
                /* canBoatInfoBeShown */ false, paywallResolver, leaderboardDTO);
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        Widget additionalWidget = super.getAdditionalWidget(dialog);
        additionalWidget.ensureDebugId("OverallLeaderboardSettingsPanel");
        return additionalWidget;
    }
    
}
