package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MultiLeaderboardPanelLifecycle extends LeaderboardPanelLifecycle {
    public static final String MID = "MultiLeaderboardPanel";

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
