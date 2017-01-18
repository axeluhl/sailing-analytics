package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveLifecycle;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public interface PlaceNavigator {
    void goToStart();

    void goToPlayer(AutoPlayerConfiguration playerConfig,

            LeaderboardWithHeaderPerspectiveLifecycle leaderboardLifecycle,
            PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings> leaderboardSettings,
            RaceBoardPerspectiveLifecycle raceboardLifecycle,
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> raceboardSettings);
}
