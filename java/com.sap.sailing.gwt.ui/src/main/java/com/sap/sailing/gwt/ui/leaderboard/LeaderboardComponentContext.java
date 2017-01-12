package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.SettingsStorageManager;

public class LeaderboardComponentContext extends AbstractLeaderboardComponentContext<LeaderboardPerspectiveLifecycle> {

    public LeaderboardComponentContext(LeaderboardPerspectiveLifecycle rootLifecycle,
            SettingsStorageManager<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> settingsStorageManager) {
        super(rootLifecycle, settingsStorageManager);
    }
}
