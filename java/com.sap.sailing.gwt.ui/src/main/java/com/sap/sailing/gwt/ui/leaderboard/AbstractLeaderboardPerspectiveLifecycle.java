package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;

public abstract class AbstractLeaderboardPerspectiveLifecycle extends AbstractPerspectiveLifecycle<LeaderboardPerspectiveOwnSettings> {

    public AbstractLeaderboardPerspectiveLifecycle(StringMessages stringMessages) {
        this(stringMessages, null);
    }
    
    public AbstractLeaderboardPerspectiveLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard) {
        addLifeCycle(new LeaderboardPanelLifecycle(leaderboard, stringMessages));
        addLifeCycle(new MultiLeaderboardPanelLifecycle(null, stringMessages));
        addLifeCycle(new OverallLeaderboardPanelLifecycle(null, stringMessages));
    }
    
    @Override
    public LeaderboardPerspectiveOwnSettings createPerspectiveOwnDefaultSettings() {
        return new LeaderboardPerspectiveOwnSettings();
    }
    
    @Override
    public LeaderboardPerspectiveOwnSettings clonePerspectiveOwnSettings(LeaderboardPerspectiveOwnSettings settings) {
        return settings;
    }

    @Override
    public SettingsDialogComponent<LeaderboardPerspectiveOwnSettings> getPerspectiveOwnSettingsDialogComponent(
            LeaderboardPerspectiveOwnSettings settings) {
        return null;
    }

    @Override
    public String getLocalizedShortName() {
        return null;
    }

    @Override
    public String getComponentId() {
        return null;
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

}
