package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MetaLeaderboardPerspectiveLifecycle extends AbstractLeaderboardPerspectiveLifecycle {
    public MetaLeaderboardPerspectiveLifecycle(StringMessages stringMessages) {
        this(stringMessages, null);
    }
    
    public MetaLeaderboardPerspectiveLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard) {
        super(stringMessages, leaderboard);
        addLifeCycle(new MultiLeaderboardPanelLifecycle(null, stringMessages));
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
}
