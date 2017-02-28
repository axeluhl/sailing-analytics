package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * This is a special lifecycle that uses a different ID, which is required to allow a MultiLeaderboard and a normal
 * Leaderboard to coexist.
 */
public class MultiLeaderboardPanelLifecycle extends LeaderboardPanelLifecycle {
    public static final String MID = "mlb";

    public MultiLeaderboardPanelLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        super(leaderboard, stringMessages);
    }

    @Override
    public String getComponentId() {
        return MID;
    }

    @Override
    public LeaderboardSettings extractContextSettings(LeaderboardSettings leaderboardSettings) {
        return createDefaultSettings();
    }

    @Override
    public String getLocalizedShortName() {
        return StringMessages.INSTANCE.regattaLeaderboards();
    }
}
