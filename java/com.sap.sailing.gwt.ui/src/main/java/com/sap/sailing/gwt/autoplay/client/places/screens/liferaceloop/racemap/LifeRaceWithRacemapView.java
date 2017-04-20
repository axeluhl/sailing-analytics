package com.sap.sailing.gwt.autoplay.client.places.screens.liferaceloop.racemap;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;

public interface LifeRaceWithRacemapView {
    void startingWith(Slide7Presenter p, AcceptsOneWidget panel, RaceMap raceMap,
            SixtyInchLeaderBoard leaderboardPanel);

    public interface Slide7Presenter {
    }

    void showErrorNoLive(LifeRaceWithRacemapPresenterImpl slide7PresenterImpl, AcceptsOneWidget panel, Throwable error);

    void onCompetitorSelect(CompetitorDTO marked);

    void scrollLeaderBoardToTop();

    void onStop();
}
