package com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;

public interface LiveRaceWithRacemapAndLeaderBoardView {
    void startingWith(Slide7Presenter p, AcceptsOneWidget panel, RaceMap raceMap,
            SixtyInchLeaderBoard leaderboardPanel);

    public interface Slide7Presenter {
    }

    void showErrorNoLive(LiveRaceWithRacemapAndLeaderBoardPresenterImpl slide7PresenterImpl, AcceptsOneWidget panel, Throwable error);

    void onCompetitorSelect(CompetitorDTO marked);

    void scrollLeaderBoardToTop();

    void onStop();
}
