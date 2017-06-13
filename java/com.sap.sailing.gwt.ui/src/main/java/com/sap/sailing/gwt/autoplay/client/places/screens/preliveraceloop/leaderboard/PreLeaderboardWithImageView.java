package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;

public interface PreLeaderboardWithImageView {
    void startingWith(Slide1Presenter p, AcceptsOneWidget panel);
    public interface Slide1Presenter {
    }

    void setLeaderBoard(SixtyInchLeaderBoard leaderboardPanel);

    void onCompetitorSelect(CompetitorDTO marked);

    void scrollLeaderBoardToTop();

    void onStop();

    void nextRace(RegattaAndRaceIdentifier race);
}
