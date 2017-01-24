package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sse.gwt.client.shared.perspective.AbstractComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.SettingsStorageManager;

public abstract class AbstractLeaderboardComponentContext<L extends AbstractLeaderboardPerspectiveLifecycle> extends
        AbstractComponentContextWithSettingsStorage<L, PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> {

    public AbstractLeaderboardComponentContext(L rootLifecycle,
            SettingsStorageManager<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> settingsStorageManager) {
        super(rootLifecycle, settingsStorageManager);
    }

}
