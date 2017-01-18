package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveLifecycle;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

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
    public void goToPlayer(AutoPlayerConfiguration playerConfig,
            LeaderboardWithHeaderPerspectiveLifecycle leaderboardLifecycle,
            PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings> leaderboardSettings,
            RaceBoardPerspectiveLifecycle raceboardLifecycle,
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> raceboardSettings) {
        PlayerPlace playerPlace = new PlayerPlace(playerConfig, leaderboardLifecycle, leaderboardSettings,
                raceboardLifecycle, raceboardSettings);
        placeController.goTo(playerPlace); 
    }

}
