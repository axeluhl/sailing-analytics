package com.sap.sailing.server.test;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithEliminations;
import com.sap.sailing.domain.leaderboard.impl.DelegatingRegattaLeaderboardWithCompetitorElimination;

public class ApplyScoresFromRaceLogTestWithEliminations extends ApplyScoresFromRaceLogTest {
    @Override
    protected RegattaLeaderboardWithEliminations createLeaderboard(Regatta regatta, int[] discardingThresholds) {
        return new DelegatingRegattaLeaderboardWithCompetitorElimination(
                ()->super.createLeaderboard(regatta, discardingThresholds), "Test leaderboard with elimination");
    }
}
