package com.sap.sailing.gwt.autoplay.client.app.sixtyinch;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.nodes.SixtyInchStartupNode;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;

public class SixtyInchPlaceNavigatorImpl implements SixtyInchPlaceNavigator {

    private PlaceController placeController;

    public SixtyInchPlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    @Override
    public void goToPlayer(String serializedSettings, AutoPlayClientFactorySixtyInch cf) {
        // setup context
        SixtyInchSetting settings = new SixtyInchSetting();
        new SettingsToStringSerializer().fromString(serializedSettings, settings);
        cf.setSlideContext(new SixtyInchContextImpl(cf.getEventBus(), settings));
        // start sixty inch slide loop nodes...
        SixtyInchStartupNode root = new SixtyInchStartupNode(cf);
        root.start(cf.getEventBus());
    }

}
