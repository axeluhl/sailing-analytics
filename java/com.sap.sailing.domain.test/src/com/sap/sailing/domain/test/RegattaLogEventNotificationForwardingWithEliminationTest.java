package com.sap.sailing.domain.test;

import java.io.Serializable;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithEliminations;
import com.sap.sailing.domain.leaderboard.impl.DelegatingRegattaLeaderboardWithCompetitorElimination;

/**
 * Serializability is required because this instance forms the outer object for the serializable
 * lambda required for the regatta leaderboard provider for the
 * {@link DelegatingRegattaLeaderboardWithCompetitorElimination} constructor call.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RegattaLogEventNotificationForwardingWithEliminationTest
        extends RegattaLogEventNotificationForwardingTest implements Serializable {
    private static final long serialVersionUID = -8184610575644911786L;

    @Override
    protected RegattaLeaderboardWithEliminations createRegattaLeaderboard(Regatta regatta) {
        return new DelegatingRegattaLeaderboardWithCompetitorElimination(
                ()->super.createRegattaLeaderboard(regatta), "Test leaderboard with elimination");
    }
}
