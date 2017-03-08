package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideContext;

public interface AutoPlayClientFactorySixtyInch extends AutoPlayClientFactory<PlaceNavigatorSixtyInch> {

    void setSlideContext(SlideContext configurationSixtyInch);

    SlideContext getSlideCtx();
}
