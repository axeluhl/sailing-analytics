package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public abstract class LeaderboardPanelLifecycle<T extends LeaderboardSettings>
        implements ComponentLifecycle<T> {
    public static final String ID = "lb";

    protected final StringMessages stringMessages;

    public LeaderboardPanelLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboard();
    }

    @Override
    public String getComponentId() {
        return ID;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
}
