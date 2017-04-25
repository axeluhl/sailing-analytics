package com.sap.sailing.gwt.autoplay.client.app.classic;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.nodes.ClassicStartupNode;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;

public class ClassicPlaceNavigatorImpl implements ClassicPlaceNavigator {

    private PlaceController placeController;

    public ClassicPlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }



    @Override
    public void goToPlayer(String serializedSettings, AutoPlayClientFactoryClassic cf) {
        // TODO: copy from sixty inch
        // PlayerPlace playerPlace = new PlayerPlace(contextAndSettings);
        // placeController.goTo(playerPlace);
        // setup context
        ClassicSetting settings = new ClassicSetting();
        new SettingsToStringSerializer().fromString(serializedSettings, settings);
        cf.setSlideContext(new ClassicContextImpl(cf.getEventBus(), settings));
        // start sixty inch slide loop nodes...
        ClassicStartupNode root = new ClassicStartupNode(cf);
        root.start(cf.getEventBus());
    }

}
