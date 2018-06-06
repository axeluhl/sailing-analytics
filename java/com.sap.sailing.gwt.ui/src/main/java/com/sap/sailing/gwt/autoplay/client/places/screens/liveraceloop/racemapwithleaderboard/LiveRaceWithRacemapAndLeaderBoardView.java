package com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.leaderboard.SingleRaceLeaderboardPanel;
import com.sap.sse.common.Distance;

public interface LiveRaceWithRacemapAndLeaderBoardView {
    void startingWith(Slide7Presenter p, AcceptsOneWidget panel, RaceMap raceMap,
            SingleRaceLeaderboardPanel leaderboardPanel);

    public interface Slide7Presenter {
    }

    void showErrorNoLive(LiveRaceWithRacemapAndLeaderBoardPresenterImpl slide7PresenterImpl, AcceptsOneWidget panel, Throwable error);

    void onCompetitorSelect(CompetitorDTO marked);

    void scrollLeaderBoardToTop();

    void onStop();

    void setStatistic(String windSpeed, Distance distance, long timeSinceStart);

    void ensureMapVisibility();
}
