package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class LeaderboardPanelLifecycle implements ComponentLifecycle<LeaderboardPanel, LeaderboardSettings, LeaderboardSettingsDialogComponent> {
    private final StringMessages stringMessages;
    private LeaderboardPanel component;
    
    public LeaderboardPanelLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.component = null;
    }
    
    @Override
    public LeaderboardSettingsDialogComponent getSettingsDialogComponent(LeaderboardSettings settings) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LeaderboardPanel getComponent() {
        return component;
    }

    @Override
    public LeaderboardSettings createDefaultSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LeaderboardSettings cloneSettings(LeaderboardSettings settings) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboard();
    }

    @Override
    public boolean hasSettings() {
        // TODO Auto-generated method stub
        return false;
    }
}

