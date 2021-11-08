package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MetaLeaderboardPerspectiveLifecycle extends AbstractLeaderboardPerspectiveLifecycle {
    
    public static final String ID = "mlbh";
    
    private final MultipleMultiLeaderboardPanelLifecycle multiLeaderboardPanelLifecycle;
    
    public MetaLeaderboardPerspectiveLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard, Iterable<DetailType> availableDetailTypes) {
        super(stringMessages, leaderboard, true, availableDetailTypes);
        multiLeaderboardPanelLifecycle = new MultipleMultiLeaderboardPanelLifecycle(stringMessages, availableDetailTypes);
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
