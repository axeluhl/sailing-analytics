package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MetaLeaderboardPerspectiveLifecycle extends AbstractLeaderboardPerspectiveLifecycle {
    
    public static final String ID = "mlbh";
    
    public MetaLeaderboardPerspectiveLifecycle(StringMessages stringMessages) {
        this(stringMessages, null);
    }
    
    public MetaLeaderboardPerspectiveLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard) {
        super(stringMessages, leaderboard, true);
        addLifeCycle(new MultiLeaderboardPanelLifecycle(null, stringMessages));
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
