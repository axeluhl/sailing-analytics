package com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;

public interface RaceEndWithBoatsView {
    void startingWith(NextRaceWithBoatsPresenter p, AcceptsOneWidget panel);
    public interface NextRaceWithBoatsPresenter {
    }

    void setLeaderBoard(SixtyInchLeaderBoard leaderboardPanel);

    void onStop();

    void setFirst(CompetitorDTO c);

    void setSecond(CompetitorDTO c);

    void setThird(CompetitorDTO c);
}
