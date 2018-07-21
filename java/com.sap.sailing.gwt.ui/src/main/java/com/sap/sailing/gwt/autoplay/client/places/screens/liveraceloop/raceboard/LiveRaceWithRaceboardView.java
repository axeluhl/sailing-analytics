package com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.raceboard;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;

public interface LiveRaceWithRaceboardView {
    void startingWith(Slide7Presenter p, AcceptsOneWidget panel, RaceBoardPanel raceBoardPanel);

    public interface Slide7Presenter {
    }

    void showErrorNoLive(LiveRaceWithRaceboardPresenterImpl slide7PresenterImpl, AcceptsOneWidget panel, Throwable error);

    void onStop();
}
