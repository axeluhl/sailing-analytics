package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;

public interface AutoPlayClientFactory extends SailingClientFactory {
    AutoPlayPlaceNavigator getPlaceNavigator();

    void setSlideContext(AutoPlayContext configurationSixtyInch);

    AutoPlayContext getSlideCtx();

    SailingDispatchSystem getDispatch();

    void startRootNode(String serializedSettings);
}
