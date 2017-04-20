package com.sap.sailing.gwt.autoplay.client.places.screens.liferaceloop.raceboard;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;

public interface LifeRaceWithRaceboardView {
    void startingWith(Slide7Presenter p, AcceptsOneWidget panel, RaceMap raceMap);

    public interface Slide7Presenter {
    }

    void showErrorNoLive(LifeRaceWithRaceboardPresenterImpl slide7PresenterImpl, AcceptsOneWidget panel, Throwable error);

    void onStop();
}
