package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.leaderboard.SingleRaceLeaderboardPanel;

public interface PreLeaderboardWithImageView {
    void startingWith(Slide1Presenter p, AcceptsOneWidget panel);
    public interface Slide1Presenter {
    }

    void setLeaderBoard(SingleRaceLeaderboardPanel leaderboardPanel);

    void onCompetitorSelect(CompetitorWithBoatDTO marked);

    void scrollLeaderBoardToTop();

    void onStop();

    void nextRace(RegattaAndRaceIdentifier race);
}
