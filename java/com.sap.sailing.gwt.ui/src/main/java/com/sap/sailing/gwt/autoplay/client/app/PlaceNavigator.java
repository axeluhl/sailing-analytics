package com.sap.sailing.gwt.autoplay.client.app;

public interface PlaceNavigator {
    void goToStart();
    void goToPlayer(String eventUuidAsString, String leaderboardIdAsNameString, String leaderboardZoom,
            boolean fullscreen, String locale);
}
