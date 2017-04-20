package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideContextImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;

public class PlaceNavigatorImpl implements PlaceNavigator, PlaceNavigatorSixtyInch {
    private final PlaceController placeController;
    private Orchestrator orchestrator;
    private AutoPlayClientFactorySixtyInch autoplayFactory;
    
    public PlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    public void setAutoplayFactory(AutoPlayClientFactorySixtyInch autoplayFactory) {
        this.autoplayFactory = autoplayFactory;
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
    public void setOrchestrator(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void goToPlayerSixtyInch(SixtyInchSetting configurationSixtyInch,
            AutoPlayClientFactorySixtyInch clientFactory) {
        
        autoplayFactory.setSlideContext(new SlideContextImpl(autoplayFactory.getEventBus(), configurationSixtyInch));
        orchestrator.start();


    }

}
