package com.sap.sailing.gwt.home.client.place.event;

import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public interface EventPageNavigator {
    void goToOverview();
    void goToRegattas();
    void goToSchedule();
    void goToMedia();
    void goToRegattaRaces(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard, RaceGroupDTO raceGroup);
    void openRaceViewer(StrippedLeaderboardDTO leaderboard, RaceDTO race);
    void openLeaderboardViewer(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard);
}
