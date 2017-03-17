package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;

public interface Slide7View {
    void startingWith(Slide7Presenter p, AcceptsOneWidget panel);

    public interface Slide7Presenter {
    }

    void setRaceMap(Widget raceboardPerspective);

    void showErrorNoLive();
}
