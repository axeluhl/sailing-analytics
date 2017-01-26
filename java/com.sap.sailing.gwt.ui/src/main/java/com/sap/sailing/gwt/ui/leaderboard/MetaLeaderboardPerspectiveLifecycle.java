package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.gwt.ui.client.StringMessages;

public class MetaLeaderboardPerspectiveLifecycle extends AbstractLeaderboardPerspectiveLifecycle {

    public MetaLeaderboardPerspectiveLifecycle(StringMessages stringMessages) {
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
