package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sse.common.TimePoint;

public class LowPointWithAutomaticRDG extends LowPoint {
    private static final long serialVersionUID = -6609749761795712461L;

    /**
     * The penalty score will be that calculated by the default in {@link LowPoint}, except for
     * {@link MaxPointsReason#RDG RDG} where all columns where the {@code competitor} has a
     * non-{@link MaxPointsReason#RDG RDG} score are averaged (including those discarded) to figure out the score to be
     * assigned to the {@link MaxPointsReason#RDG RDG} redress.
     */
    @Override
    public Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason,
            Integer numberOfCompetitorsInRace,
            NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint,
            Leaderboard leaderboard) {
        final Double result;
        if (maxPointsReason == MaxPointsReason.RDG) {
            double scoreSum = 0;
            int numberOfScores = 0;
            for (final RaceColumn rc : leaderboard.getRaceColumns()) {
                if (rc != raceColumn && leaderboard.getMaxPointsReason(competitor, rc, timePoint) != MaxPointsReason.RDG) {
                    final Double totalPointsInRC = leaderboard.getTotalPoints(competitor, rc, timePoint);
                    if (totalPointsInRC != null) {
                        scoreSum += totalPointsInRC;
                        numberOfScores++;
                    }
                }
            }
            if (numberOfScores == 0) {
                // if only RDG races exist, leave null
                result = null;
            } else {
                result = scoreSum / (double) numberOfScores;
            }
        } else {
            result = super.getPenaltyScore(raceColumn, competitor, maxPointsReason, numberOfCompetitorsInRace,
                    numberOfCompetitorsInLeaderboardFetcher, timePoint, leaderboard);
        }
        return result;
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_WITH_AUTOMATIC_RDG;
    }

}
