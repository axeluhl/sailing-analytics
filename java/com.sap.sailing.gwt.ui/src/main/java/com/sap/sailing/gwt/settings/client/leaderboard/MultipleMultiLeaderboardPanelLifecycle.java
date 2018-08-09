package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.Collections;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * This is a special lifecycle that uses a different ID, which is required to allow a MultiLeaderboard and a normal
 * Leaderboard to coexist.
 */
public class MultipleMultiLeaderboardPanelLifecycle extends MultiRaceLeaderboardPanelLifecycle {
    public static final String MID = "mlb";

    public MultipleMultiLeaderboardPanelLifecycle(StringMessages stringMessages, Iterable<DetailType> availableDetailTypes) {
        super(Collections.emptyList(), stringMessages, availableDetailTypes);
    }

    @Override
    public String getComponentId() {
        return MID;
    }

    @Override
    public MultiRaceLeaderboardSettings extractDocumentSettings(MultiRaceLeaderboardSettings leaderboardSettings) {
        return createDefaultSettings();
    }

    @Override
    public String getLocalizedShortName() {
        return StringMessages.INSTANCE.regattaLeaderboards();
    }
}
