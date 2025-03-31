package com.sap.sailing.server.test;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithEliminations;
import com.sap.sailing.domain.leaderboard.impl.DelegatingRegattaLeaderboardWithCompetitorElimination;

public class ApplyScoresFromRaceLogWithEliminationsTest extends ApplyScoresFromRaceLogTest {
    @Override
    protected RegattaLeaderboardWithEliminations createLeaderboard(Regatta regatta, int[] discardingThresholds) {
        final RegattaLeaderboard regattaLeaderboard = super.createLeaderboard(regatta, discardingThresholds);
        service.addLeaderboard(regattaLeaderboard);
        return new DelegatingRegattaLeaderboardWithCompetitorElimination(
                ()->regattaLeaderboard, "Test leaderboard with elimination");
    }
}
