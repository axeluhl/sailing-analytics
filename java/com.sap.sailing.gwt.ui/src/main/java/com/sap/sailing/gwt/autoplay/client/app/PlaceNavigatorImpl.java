package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl.AutoPlayLoopNode;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideContextImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.IdleUpNextNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.LifeRaceWithRacemapNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.PreRaceWithRacemapNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.RaceEndWithBoatsNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.SixtyInchRootNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.StartupNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;

public class PlaceNavigatorImpl implements PlaceNavigator, PlaceNavigatorSixtyInch {
    private final PlaceController placeController;

    
    public PlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    @Override
    public void goToStart() {
        placeController.goTo(new StartPlace()); 
    }

    @Override
    public void goToPlayer(String contextAndSettings) {
        PlayerPlace playerPlace = new PlayerPlace(contextAndSettings);
        placeController.goTo(playerPlace); 
    }



    @Override
    public void goToPlayerSixtyInch(SixtyInchSetting configurationSixtyInch,
            AutoPlayClientFactorySixtyInch cf) {
        
        cf.setSlideContext(new SlideContextImpl(cf.getEventBus(), configurationSixtyInch));

        AutoPlayLoopNode idleLoop = new AutoPlayLoopNode(30, new IdleUpNextNode(cf));
        AutoPlayLoopNode preLifeRaceLoop = new AutoPlayLoopNode(30, new PreRaceWithRacemapNode(cf));
        AutoPlayLoopNode lifeRaceLoop = new AutoPlayLoopNode(30, new LifeRaceWithRacemapNode(cf));
        AutoPlayLoopNode afterLifeRaceLoop = new AutoPlayLoopNode(30, new RaceEndWithBoatsNode(cf), idleLoop);
        SixtyInchRootNode raceLoop = new SixtyInchRootNode(cf, idleLoop, lifeRaceLoop, preLifeRaceLoop,
                afterLifeRaceLoop);
        StartupNode root = new StartupNode(cf);
        root.setWhenReadyDestination(raceLoop);
        root.start(cf.getEventBus());
    }

}
