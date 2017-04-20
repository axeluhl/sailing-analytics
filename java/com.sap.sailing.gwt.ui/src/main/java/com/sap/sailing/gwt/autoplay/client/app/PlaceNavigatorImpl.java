package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SixtyInchContextImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.nodes.StartupNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.start.SixtyInchSetting;
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
        // setup context
        cf.setSlideContext(new SixtyInchContextImpl(cf.getEventBus(), configurationSixtyInch));
        // start sixty inch slide loop nodes...
        StartupNode root = new StartupNode(cf);
        root.start(cf.getEventBus());
    }

}
