package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;

public interface Slide7View {
    void startingWith(Slide7Presenter p, AcceptsOneWidget panel, RaceMap raceMap);

    public interface Slide7Presenter {
    }

    void showErrorNoLive(Slide7PresenterImpl slide7PresenterImpl, AcceptsOneWidget panel, Throwable error);
}
