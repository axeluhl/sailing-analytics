package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class LeaderboardPerspectiveLifecycle extends AbstractLeaderboardPerspectiveLifecycle {
    
    public static final String ID = "lbh";
    
    public LeaderboardPerspectiveLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard) {
        super(stringMessages, leaderboard, false);
        addLifeCycle(new OverallLeaderboardPanelLifecycle(null, stringMessages));
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public String getComponentId() {
        return ID;
    }

}
