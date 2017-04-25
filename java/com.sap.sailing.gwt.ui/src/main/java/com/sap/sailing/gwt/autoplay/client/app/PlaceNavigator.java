package com.sap.sailing.gwt.autoplay.client.app;

public interface PlaceNavigator<CF extends AutoPlayClientFactory<?>> {

    void goToPlayer(String contextAndSettings, CF clientFactory);
}
