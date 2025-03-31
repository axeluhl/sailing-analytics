package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class MetaLeaderboardPerspectiveLifecycle extends AbstractLeaderboardPerspectiveLifecycle {
    
    public static final String ID = "mlbh";
    
    private final MultipleMultiLeaderboardPanelLifecycle multiLeaderboardPanelLifecycle;
    
    public MetaLeaderboardPerspectiveLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard, Iterable<DetailType> availableDetailTypes, PaywallResolver paywallResolver) {
        super(stringMessages, leaderboard, true, availableDetailTypes, paywallResolver);
        multiLeaderboardPanelLifecycle = new MultipleMultiLeaderboardPanelLifecycle(stringMessages, availableDetailTypes, paywallResolver, leaderboard);
        addLifeCycle(multiLeaderboardPanelLifecycle);
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public String getComponentId() {
        return ID;
    }
    
    public MultipleMultiLeaderboardPanelLifecycle getMultiLeaderboardPanelLifecycle() {
        return multiLeaderboardPanelLifecycle;
    }
}
