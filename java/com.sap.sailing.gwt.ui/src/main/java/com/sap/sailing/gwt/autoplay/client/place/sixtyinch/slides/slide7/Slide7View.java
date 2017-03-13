package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;

public interface Slide7View {
    void startingWith(Slide7Presenter p, AcceptsOneWidget panel);

    public interface Slide7Presenter {
    }

    void setRaceMap(RaceBoardPanel raceboardPerspective);

    void showErrorNoLive();
}
