package com.sap.sailing.gwt.autoplay.client.app.classic;

import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;

public interface AutoPlayClientFactoryClassic extends AutoPlayClientFactory<PlaceNavigator> {

    void setSlideContext(ClassicContext configurationSixtyInch);

    ClassicContext getSlideCtx();

    SailingDispatchSystem getDispatch();
}
