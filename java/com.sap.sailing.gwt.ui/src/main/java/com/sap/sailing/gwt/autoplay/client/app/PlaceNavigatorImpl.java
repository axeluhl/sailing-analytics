package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.autoplay.client.place.player.LeaderboardWithHeaderPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveLifecycle;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndComponentSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndSettings;

public class PlaceNavigatorImpl implements PlaceNavigator {
    private final PlaceController placeController;
    
    protected PlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    @Override
    public void goToStart() {
        placeController.goTo(new StartPlace()); 
    }
    
    @Override
    public void goToPlayer(
            AutoPlayerConfiguration playerConfig,PerspectiveLifecycleAndSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> leaderboardPerspectiveAndSettings, 
            PerspectiveLifecycleAndSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> raceboardPerspectiveAndSettings,
            PerspectiveLifecycleAndComponentSettings<LeaderboardWithHeaderPerspectiveLifecycle> leaderboardPerspectiveComponentLifecyclesAndSettings,
            PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> raceboardPerspectiveComponentLifecyclesAndSettings) {
        PlayerPlace playerPlace = new PlayerPlace(playerConfig, leaderboardPerspectiveAndSettings, raceboardPerspectiveAndSettings,
                leaderboardPerspectiveComponentLifecyclesAndSettings, raceboardPerspectiveComponentLifecyclesAndSettings);
        placeController.goTo(playerPlace); 
    }

}
