package com.sap.sailing.gwt.autoplay.client.app.sixtyinch;

import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;

public interface AutoPlayClientFactorySixtyInch extends AutoPlayClientFactory<PlaceNavigatorSixtyInch> {

    void setSlideContext(SixtyInchContext configurationSixtyInch);

    SixtyInchContext getSlideCtx();

    SailingDispatchSystem getDispatch();
}
