package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchOrchestrator;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;

public interface PlaceNavigatorSixtyInch extends PlaceNavigator {

    void setAutoplayFactory(AutoPlayClientFactorySixtyInch autoPlayClientFactorySixtyInchImpl);

    void goToPlayerSixtyInch(SixtyInchSetting configurationSixtyInch, AutoPlayClientFactorySixtyInch clientFactory);

    void setOrchestrator(SixtyInchOrchestrator orchestrator);
}
