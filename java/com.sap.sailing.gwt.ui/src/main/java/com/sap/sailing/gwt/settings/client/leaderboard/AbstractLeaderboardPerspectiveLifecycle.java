package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;

public abstract class AbstractLeaderboardPerspectiveLifecycle extends AbstractPerspectiveLifecycle<LeaderboardPerspectiveOwnSettings> {

    protected AbstractLeaderboardPerspectiveLifecycle(StringMessages stringMessages,
            AbstractLeaderboardDTO leaderboard, boolean isOverall, Iterable<DetailType> availableDetailTypes) {
        addLifeCycle(new MultiRaceLeaderboardPanelLifecycle(leaderboard, stringMessages, availableDetailTypes));
        addLifeCycle(new MultiCompetitorLeaderboardChartLifecycle(isOverall));
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
    protected LeaderboardPerspectiveOwnSettings extractOwnUserSettings(LeaderboardPerspectiveOwnSettings settings) {
        return settings;
    }

    @Override
    protected LeaderboardPerspectiveOwnSettings extractOwnDocumentSettings(LeaderboardPerspectiveOwnSettings settings) {
        return settings;
    }

    @Override
    public String getLocalizedShortName() {
        return StringMessages.INSTANCE.leaderboardPage();
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

}
