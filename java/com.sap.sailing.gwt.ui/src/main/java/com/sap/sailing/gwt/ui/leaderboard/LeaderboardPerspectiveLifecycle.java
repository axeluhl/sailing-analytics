package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.gwt.ui.client.StringMessages;

public class LeaderboardPerspectiveLifecycle extends AbstractLeaderboardPerspectiveLifecycle {

    public LeaderboardPerspectiveLifecycle(StringMessages stringMessages) {
        super(stringMessages);
    }

    @Override
    protected LeaderboardPerspectiveOwnSettings extractOwnGlobalSettings(LeaderboardPerspectiveOwnSettings settings) {
        throw new IllegalStateException("Not supported");
    }

    @Override
    protected LeaderboardPerspectiveOwnSettings extractOwnContextSettings(LeaderboardPerspectiveOwnSettings settings) {
        throw new IllegalStateException("Not supported");
    }

}
