package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.autoplay.client.place.player.LeaderboardWithHeaderPerspective;
import com.sap.sailing.gwt.ui.raceboard.ProxyRaceBoardPerspective;

public interface PlaceNavigator {
    void goToStart();
    void goToPlayer(AutoPlayerConfiguration playerConfig, LeaderboardWithHeaderPerspective leaderboardPerspective, 
            ProxyRaceBoardPerspective raceboardPerspective);
}
