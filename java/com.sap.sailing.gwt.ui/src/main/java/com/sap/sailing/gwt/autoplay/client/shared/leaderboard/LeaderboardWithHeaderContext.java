package com.sap.sailing.gwt.autoplay.client.shared.leaderboard;

import com.sap.sse.gwt.client.shared.perspective.ComponentContext;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class LeaderboardWithHeaderContext extends ComponentContext<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> {

    public LeaderboardWithHeaderContext(String entryPointId, LeaderboardWithHeaderPerspectiveLifecycle rootPerspectiveLifecycle) {
        super(entryPointId, rootPerspectiveLifecycle);
        // TODO Auto-generated constructor stub
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
