package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class LeaderboardPerspectiveLifecycle extends AbstractLeaderboardPerspectiveLifecycle {
    public static final String ID = "LeaderboardPerspectiveLifecycle";

    public LeaderboardPerspectiveLifecycle(StringMessages stringMessages) {
        super(stringMessages);
    }
    
    public LeaderboardPerspectiveLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard) {
        super(stringMessages, leaderboard);
    }

    @Override
    protected LeaderboardPerspectiveOwnSettings extractOwnGlobalSettings(LeaderboardPerspectiveOwnSettings settings) {
        return new LeaderboardPerspectiveOwnSettings(settings.isShowRaceDetails(), settings.isHideToolbar(),
                settings.isAutoExpandLastRaceColumn(), settings.isShowCharts(), settings.isShowOverallLeaderboard(),
                settings.isShowSeriesLeaderboards());
    }

    @Override
    protected LeaderboardPerspectiveOwnSettings extractOwnContextSettings(LeaderboardPerspectiveOwnSettings settings) {
        LeaderboardPerspectiveOwnSettings defaultSet = createPerspectiveOwnDefaultSettings();
        return defaultSet;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
}
