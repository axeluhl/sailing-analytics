package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;

public abstract class AbstractLeaderboardPerspectiveLifecycle extends AbstractPerspectiveLifecycle<LeaderboardPerspectiveOwnSettings> {

    private final LeaderboardPanelLifecycle leaderboardPanelLifecycle;

    public AbstractLeaderboardPerspectiveLifecycle(StringMessages stringMessages) {
        leaderboardPanelLifecycle = new LeaderboardPanelLifecycle(null, stringMessages);
        componentLifecycles.add(leaderboardPanelLifecycle);
    }
    
    @Override
    public LeaderboardPerspectiveOwnSettings createPerspectiveOwnDefaultSettings() {
        return new LeaderboardPerspectiveOwnSettings();
    }
    
    public LeaderboardPanelLifecycle getLeaderboardPanelLifecycle() {
        return leaderboardPanelLifecycle;
    }

    @Override
    public LeaderboardPerspectiveOwnSettings clonePerspectiveOwnSettings(LeaderboardPerspectiveOwnSettings settings) {
        // TODO Auto-generated method stub
        return settings;
    }

    @Override
    public SettingsDialogComponent<LeaderboardPerspectiveOwnSettings> getPerspectiveOwnSettingsDialogComponent(
            LeaderboardPerspectiveOwnSettings settings) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalizedShortName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getComponentId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasSettings() {
        // TODO Auto-generated method stub
        return false;
    }

}
