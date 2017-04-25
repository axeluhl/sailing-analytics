package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.PlaceNavigatorSixtyInch;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.SixtyInchContextImpl;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.SixtyInchSetting;
import com.sap.sailing.gwt.autoplay.client.nodes.SixtyInchStartupNode;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.config.ClassicConfigPlace;

public class PlaceNavigatorImpl implements PlaceNavigator, PlaceNavigatorSixtyInch {
    private final PlaceController placeController;

    
    public PlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    @Override
    public void goToStart() {
        placeController.goTo(new ClassicConfigPlace()); 
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
        SixtyInchStartupNode root = new SixtyInchStartupNode(cf);
        root.start(cf.getEventBus());
    }

}
