package com.sap.sailing.gwt.home.client.place.event;

import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public interface EventPageNavigator {
    void goToOverview();
    void goToRegattas();
    void goToSchedule();
    void goToMedia();
    void goToRegattaRaces(StrippedLeaderboardDTO leaderboard);
}
