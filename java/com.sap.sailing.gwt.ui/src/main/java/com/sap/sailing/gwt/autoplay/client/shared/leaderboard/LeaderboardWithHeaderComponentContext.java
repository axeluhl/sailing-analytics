package com.sap.sailing.gwt.autoplay.client.shared.leaderboard;

import com.sap.sse.gwt.client.shared.perspective.ComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.settings.UserSettingsStorageManager;

public class LeaderboardWithHeaderComponentContext extends
        ComponentContextWithSettingsStorage<PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings>> {

    public LeaderboardWithHeaderComponentContext(UserService userService, String entryPointId, LeaderboardWithHeaderPerspectiveLifecycle rootPerspectiveLifecycle) {
        super(rootPerspectiveLifecycle, new UserSettingsStorageManager<PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings>>(userService, entryPointId, "global"));
    }

}
