package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class OverallLeaderboardPanelLifecycle extends LeaderboardPanelLifecycle {

    protected static final String ID = "OverallLeaderboardPanel";

    public OverallLeaderboardPanelLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        super(leaderboard, stringMessages);
    }

    @Override
    public String getComponentId() {
        return ID;
    }

    @Override
    public String getLocalizedShortName() {
        return StringMessages.INSTANCE.overallLeaderboard();
    }

}
