package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class LeaderboardPerspectiveLifecycle extends AbstractLeaderboardPerspectiveLifecycle {
    public LeaderboardPerspectiveLifecycle(StringMessages stringMessages) {
        this(stringMessages, null);
    }
    
    public LeaderboardPerspectiveLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard) {
        super(stringMessages, leaderboard, false);
        addLifeCycle(new OverallLeaderboardPanelLifecycle(null, stringMessages));
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

}
