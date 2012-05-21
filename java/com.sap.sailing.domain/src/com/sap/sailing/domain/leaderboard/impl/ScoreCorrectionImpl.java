package com.sap.sailing.domain.leaderboard.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.RaceColumn;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Implements the basic logic of assigning a maximum score to a competitor in a race if that competitor was
 * disqualified, did not start or did not finish. The maximum score is determined by counting the number of competitors
 * listed in the regatta to which the race belongs.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class ScoreCorrectionImpl implements SettableScoreCorrection {
    private static final long serialVersionUID = -7088305215528928135L;

    /**
     * If no max point reason is provided for a competitor/race, {@link MaxPointsReason#NONE} should be the default.
     */
    private final Map<Pair<Competitor, RaceColumn>, MaxPointsReason> maxPointsReasons;

    /**
     * If no score correction is provided here, the uncorrected points are the default.
     */
    private final Map<Pair<Competitor, RaceColumn>, Integer> correctedScores;
    
    public ScoreCorrectionImpl() {
        this.maxPointsReasons = new HashMap<Util.Pair<Competitor,RaceColumn>, MaxPointsReason>();
        this.correctedScores = new HashMap<Util.Pair<Competitor,RaceColumn>, Integer>();
    }

    @Override
    public void setMaxPointsReason(Competitor competitor, RaceColumn raceColumn, MaxPointsReason reason) {
        Pair<Competitor, RaceColumn> key = raceColumn.getKey(competitor);
        if (reason == null) {
            maxPointsReasons.remove(key);
        } else {
            maxPointsReasons.put(key, reason);
        }
    }

    @Override
    public void correctScore(Competitor competitor, RaceColumn raceColumn, int points) {
        correctedScores.put(raceColumn.getKey(competitor), points);
    }
    
    @Override
    public boolean isScoreCorrected(Competitor competitor, RaceColumn raceColumn) {
        Pair<Competitor, RaceColumn> key = raceColumn.getKey(competitor);
        return correctedScores.containsKey(key) || maxPointsReasons.containsKey(key);
    }
    
    @Override
    public void uncorrectScore(Competitor competitor, RaceColumn raceColumn) {
        correctedScores.remove(raceColumn.getKey(competitor));
    }

    @Override
    public MaxPointsReason getMaxPointsReason(Competitor competitor, RaceColumn raceColumn) {
        MaxPointsReason result = maxPointsReasons.get(raceColumn.getKey(competitor));
        if (result == null) {
            result = MaxPointsReason.NONE;
        }
        return result;
    }
    
    /**
     * If the {@link #getMaxPointsReason(Competitor, TrackedRace)} for the <code>competitor</code> for the
     * <code>raceColumn</code>'s tracked race is not {@link MaxPointsReason#NONE}, the
     * {@link #getMaxPoints(TrackedRace) maximum score} is computed for the competitor. Otherwise, the
     * <code>uncorrectedScore</code> is returned.
     * <p>
     */
    @Override
    public Result getCorrectedScore(int uncorrectedScore, final Competitor competitor, final RaceColumn raceColumn,
            TimePoint timePoint, int numberOfCompetitorsInLeaderboard) {
        int result;
        final MaxPointsReason maxPointsReason = getMaxPointsReason(competitor, raceColumn);
        if (maxPointsReason == MaxPointsReason.NONE) {
            result = getCorrectedNonMaxedScore(competitor, raceColumn, uncorrectedScore);
        } else {
            // allow explicit override even when max points reason is specified; calculation may be wrong,
            // e.g., in case we have an untracked race and the number of competitors is estimated incorrectly
            Integer correctedNonMaxedScore = correctedScores.get(raceColumn.getKey(competitor));
            if (correctedNonMaxedScore == null) {
                result = getMaxPoints(raceColumn.getTrackedRace(competitor), numberOfCompetitorsInLeaderboard);
            } else {
                result = correctedNonMaxedScore;
            }
        }
        final int correctedScore = result;
        return new Result() {
            @Override
            public MaxPointsReason getMaxPointsReason() {
                return maxPointsReason;
            }
            
            @Override
            public int getCorrectedScore() {
                return correctedScore;
            }

            @Override
            public boolean isCorrected() {
                return isScoreCorrected(competitor, raceColumn);
            }
        };
    }

    /**
     * Under the assumption that the competitor is not assigned the maximum score due to disqualification or other
     * reasons, computes the corrected score. This default implementation uses <code>uncorrectedScore</code> without
     * changes. Subclasses may wish to allow for optionally overwriting this uncorrected score to handle, e.g.,
     * differences between what the tracking results suggest and what the jury or race committee decided.
     */
    protected int getCorrectedNonMaxedScore(Competitor competitor, RaceColumn raceColumn, int uncorrectedScore) {
        Integer correctedNonMaxedScore = correctedScores.get(raceColumn.getKey(competitor));
        if (correctedNonMaxedScore == null) {
            return uncorrectedScore;
        } else {
            return correctedNonMaxedScore;
        }
    }

    private int getMaxPoints(TrackedRace trackedRace, int numberOfCompetitorsInLeaderboard) {
        return trackedRace == null ? numberOfCompetitorsInLeaderboard+1 : Util.size(trackedRace.getRace().getCompetitors())+1;
    }

    @Override
    public Integer getExplicitScoreCorrection(Competitor competitor, RaceColumn raceColumn) {
        return correctedScores.get(raceColumn.getKey(competitor));
    }

    @Override
    public boolean hasCorrectionFor(RaceColumn raceInLeaderboard) {
        for (Pair<Competitor, RaceColumn> correctedScoresKey : correctedScores.keySet()) {
            if (correctedScoresKey.getB() == raceInLeaderboard) {
                return true;
            }
        }
        for (Pair<Competitor, RaceColumn> maxPointsReasonsKey : maxPointsReasons.keySet()) {
            if (maxPointsReasonsKey.getB() == raceInLeaderboard) {
                return true;
            }
        }
        return false;
    }

}
