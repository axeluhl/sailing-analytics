package com.sap.sailing.gwt.autoplay.client.shared.leaderboard;

import com.sap.sse.gwt.client.shared.perspective.AbstractComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.settings.UserSettingsStorageManager;

public class LeaderboardWithHeaderComponentContext extends AbstractComponentContextWithSettingsStorage<LeaderboardWithHeaderPerspectiveLifecycle, PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings>> {

    public LeaderboardWithHeaderComponentContext(UserService userService, String entryPointId, LeaderboardWithHeaderPerspectiveLifecycle rootPerspectiveLifecycle) {
        super(rootPerspectiveLifecycle, new UserSettingsStorageManager<PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings>>(userService, entryPointId, "global"));
    }

}
