package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;

public abstract class AbstractLeaderboardPerspectiveLifecycle extends AbstractPerspectiveLifecycle<LeaderboardPerspectiveOwnSettings> {

    protected AbstractLeaderboardPerspectiveLifecycle(StringMessages stringMessages,
            AbstractLeaderboardDTO leaderboard) {
        addLifeCycle(new LeaderboardPanelLifecycle(leaderboard, stringMessages));
    }
    
    @Override
    public LeaderboardPerspectiveOwnSettings createPerspectiveOwnDefaultSettings() {
        return new LeaderboardPerspectiveOwnSettings();
    }
    
    @Override
    public SettingsDialogComponent<LeaderboardPerspectiveOwnSettings> getPerspectiveOwnSettingsDialogComponent(
            LeaderboardPerspectiveOwnSettings settings) {
        return new LeaderboardPerspectiveOwnSettingsDialogComponent(settings);
    }

    @Override
    protected LeaderboardPerspectiveOwnSettings extractOwnGlobalSettings(LeaderboardPerspectiveOwnSettings settings) {
        return settings;
    }

    @Override
    protected LeaderboardPerspectiveOwnSettings extractOwnContextSettings(LeaderboardPerspectiveOwnSettings settings) {
        LeaderboardPerspectiveOwnSettings defaultSet = createPerspectiveOwnDefaultSettings();
        return defaultSet;
    }

    @Override
    public String getLocalizedShortName() {
        return StringMessages.INSTANCE.leaderboardPage();
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
