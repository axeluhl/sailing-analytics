package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycleAndSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndComponentSettings;

public interface PlaceNavigator {
    void goToStart();
    void goToPlayer(AutoPlayerConfiguration playerConfig, ComponentLifecycleAndSettings<LeaderboardPerspectiveSettings> leaderboardPerspectiveAndSettings, 
            ComponentLifecycleAndSettings<RaceBoardPerspectiveSettings> raceboardPerspectiveAndSettings,
            PerspectiveLifecycleAndComponentSettings leaderboardPerspectiveComponentLifecyclesAndSettings,
            PerspectiveLifecycleAndComponentSettings raceboardPerspectiveComponentLifecyclesAndSettings);
}
