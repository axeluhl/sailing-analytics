package com.sap.sailing.domain.test;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithEliminations;
import com.sap.sailing.domain.leaderboard.impl.DelegatingRegattaLeaderboardWithCompetitorElimination;

public class RegattaLogEventNotificationForwardingWithEliminationTest
        extends RegattaLogEventNotificationForwardingTest {
    @Override
    protected RegattaLeaderboardWithEliminations createRegattaLeaderboard(Regatta regatta) {
        return new DelegatingRegattaLeaderboardWithCompetitorElimination(
                ()->super.createRegattaLeaderboard(regatta), "Test leaderboard with elimination");
    }
}
