package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;

public interface IdleOverallLeaderBoardView {
    void startingWith(Slide7Presenter p, AcceptsOneWidget panel, SixtyInchLeaderBoard leaderboardPanel);

    public interface Slide7Presenter {
    }

    void onStop();
}
