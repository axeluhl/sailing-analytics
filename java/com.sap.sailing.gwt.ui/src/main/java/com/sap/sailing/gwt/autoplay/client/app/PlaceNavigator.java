package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.autoplay.client.place.player.LeaderboardWithHeaderPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.place.player.RaceBoardPerspectiveLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndComponentSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndSettings;

public interface PlaceNavigator {
    void goToStart();
    void goToPlayer(AutoPlayerConfiguration playerConfig, PerspectiveLifecycleAndSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> leaderboardPerspectiveAndSettings, 
            PerspectiveLifecycleAndSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> raceboardPerspectiveAndSettings,
            PerspectiveLifecycleAndComponentSettings<LeaderboardWithHeaderPerspectiveLifecycle> leaderboardPerspectiveComponentLifecyclesAndSettings,
            PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> raceboardPerspectiveComponentLifecyclesAndSettings);
}
