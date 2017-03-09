package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchOrchestrator;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideContextImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveLifecycle;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleWithAllSettings;

public class PlaceNavigatorImpl implements PlaceNavigator, PlaceNavigatorSixtyInch {
    private final PlaceController placeController;
    private SixtyInchOrchestrator orchestrator;
    private AutoPlayClientFactorySixtyInch autoplayFactory;
    
    public PlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    public void setAutoplayFactory(AutoPlayClientFactorySixtyInch autoplayFactory) {
        this.autoplayFactory = autoplayFactory;
    }

    @Override
    public void goToPlayer(
            AutoPlayerConfiguration playerConfig, 
            PerspectiveLifecycleWithAllSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> leaderboardPerspectiveLifecycleWithAllSettings,
            PerspectiveLifecycleWithAllSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> raceboardPerspectiveLifecyclesWithAllSettings) {
        PlayerPlace playerPlace = new PlayerPlace(playerConfig, leaderboardPerspectiveLifecycleWithAllSettings, raceboardPerspectiveLifecyclesWithAllSettings);
        placeController.goTo(playerPlace); 
    }

    @Override
    public void setOrchestrator(SixtyInchOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void goToPlayerSixtyInch(SixtyInchSetting configurationSixtyInch,
            AutoPlayClientFactorySixtyInch clientFactory) {
        
        autoplayFactory.setSlideContext(new SlideContextImpl(configurationSixtyInch));
        orchestrator.start();
        placeController.goTo(new SlideInitPlace());

    }

}
