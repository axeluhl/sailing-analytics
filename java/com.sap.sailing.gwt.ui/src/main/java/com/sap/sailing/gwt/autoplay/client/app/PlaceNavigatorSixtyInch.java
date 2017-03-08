package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchOrchestrator;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideContext;

public interface PlaceNavigatorSixtyInch extends PlaceNavigator {

    void setAutoplayFactory(AutoPlayClientFactorySixtyInch autoPlayClientFactorySixtyInchImpl);

    void goToPlayerSixtyInch(SlideContext configurationSixtyInch);

    void setOrchestrator(SixtyInchOrchestrator orchestrator);
}
