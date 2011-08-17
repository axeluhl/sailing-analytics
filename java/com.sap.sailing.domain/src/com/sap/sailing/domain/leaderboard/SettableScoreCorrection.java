package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface SettableScoreCorrection extends ScoreCorrection {

    void setMaxPointsReason(Competitor competitor, RaceInLeaderboard raceColumn, MaxPointsReason reason);

    void correctScore(Competitor competitor, RaceInLeaderboard raceColumn, int points);

    /**
     * Removes a score correction which makes the competitor's score for <code>race</code> to fall back to the score
     * determined by the tracking data.
     */
    void uncorrectScore(Competitor competitor, TrackedRace race);

}
