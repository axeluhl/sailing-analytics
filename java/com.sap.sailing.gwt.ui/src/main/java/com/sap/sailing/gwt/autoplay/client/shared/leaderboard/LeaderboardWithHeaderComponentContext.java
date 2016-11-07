package com.sap.sailing.gwt.autoplay.client.shared.leaderboard;

import com.sap.sse.gwt.client.shared.perspective.AbstractComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.UserSettingsStorageManager;

public class LeaderboardWithHeaderComponentContext extends AbstractComponentContextWithSettingsStorage<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> {

    public LeaderboardWithHeaderComponentContext(String entryPointId, LeaderboardWithHeaderPerspectiveLifecycle rootPerspectiveLifecycle) {
        super(rootPerspectiveLifecycle, new UserSettingsStorageManager<LeaderboardWithHeaderPerspectiveSettings>(entryPointId));
    }

    @Override
    protected PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings> extractContextSpecificSettings(
            PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings> newRootPerspectiveSettings) {
        // TODO Auto-generated method stub
        return newRootPerspectiveSettings;
    }

    @Override
    protected PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings> extractGlobalSettings(
            PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings> newRootPerspectiveSettings) {
        // TODO Auto-generated method stub
        return newRootPerspectiveSettings;
    }

}
