package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerClientFactory;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;

public interface AutoPlayClientFactory extends PlayerClientFactory {
    AutoPlayPlaceNavigator getPlaceNavigator();

    void setSlideContext(AutoPlayContext configurationSixtyInch);

    AutoPlayContext getSlideCtx();

    SailingDispatchSystem getDispatch();
}
