package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class OverallLeaderboardPanelLifecycle extends LeaderboardPanelLifecycle {

    public static final String ID = "olpl";

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
