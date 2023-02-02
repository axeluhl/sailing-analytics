package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class LeaderboardPerspectiveLifecycle extends AbstractLeaderboardPerspectiveLifecycle {
    
    public static final String ID = "lbh";
    
    public LeaderboardPerspectiveLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard, Iterable<DetailType> availableDetailTypes,
            PaywallResolver paywallResolver) {
        super(stringMessages, leaderboard, false, availableDetailTypes, paywallResolver);
        addLifeCycle(new OverallLeaderboardPanelLifecycle(leaderboard, stringMessages, availableDetailTypes, paywallResolver));
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
