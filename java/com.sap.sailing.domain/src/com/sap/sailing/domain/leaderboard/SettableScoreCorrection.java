package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;

public interface SettableScoreCorrection extends ScoreCorrection {

    void setMaxPointsReason(Competitor competitor, RaceInLeaderboard raceColumn, MaxPointsReason reason);
    
    MaxPointsReason getMaxPointsReason(Competitor competitor, RaceInLeaderboard raceColumn);

    void correctScore(Competitor competitor, RaceInLeaderboard raceColumn, int points);

    /**
     * Removes a score correction which makes the competitor's score for <code>raceColumn</code> to fall back to the score
     * determined by the tracking data.
     */
    void uncorrectScore(Competitor competitor, RaceInLeaderboard raceColumn);
    
    /**
     * @return <code>null</code> if not set for the competitor, e.g., because no correction was made or an explicit
     *         {@link MaxPointsReason} was provided for the competitor.
     */
    Integer getExplicitScoreCorrection(Competitor competitor, RaceInLeaderboard raceColumn);

}
