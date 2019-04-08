package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;

public interface IdleSixtyInchLeaderboardView {
    void startingWith(Slide7Presenter p, AcceptsOneWidget panel, MultiRaceLeaderboardPanel leaderboardPanel);

    public interface Slide7Presenter {
    }

    void onStop();

    void scrollLeaderBoardToTop();

    void scrollIntoView(int selected);
}
