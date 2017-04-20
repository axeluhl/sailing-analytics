package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SixtyInchContext;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;

public interface AutoPlayClientFactorySixtyInch extends AutoPlayClientFactory<PlaceNavigatorSixtyInch> {

    void setSlideContext(SixtyInchContext configurationSixtyInch);

    SixtyInchContext getSlideCtx();

    SailingDispatchSystem getDispatch();
}
