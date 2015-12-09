package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;

public interface PlaceNavigator {
    void goToStart();
    void goToPlayer(AutoPlayerConfiguration playerConfig, Pair<LeaderboardPerspectiveSettings, CompositeSettings> leaderboardPerspectiveSettings,
            Pair<RaceBoardPerspectiveSettings, CompositeSettings> raceboardPerspectiveSettings);
}
