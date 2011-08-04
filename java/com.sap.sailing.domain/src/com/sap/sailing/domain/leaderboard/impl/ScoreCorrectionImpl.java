package com.sap.sailing.domain.leaderboard.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.leaderboard.ScoreCorrection;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.Util;
import com.sap.sailing.util.Util.Pair;

/**
 * Implements the basic logic of assigning a maximum score to a competitor in a race if that competitor was
 * disqualified, did not start or did not finish. The maximum score is determined by counting the number of competitors
 * listed in the event to which the race belongs.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class ScoreCorrectionImpl implements SettableScoreCorrection {
    /**
     * If no max point reason is provided for a competitor/race, {@link MaxPointsReason#NONE} should be the default.
     */
    private final Map<Pair<Competitor, TrackedRace>, MaxPointsReason> maxPointsReasons;

    /**
     * If no score correction is provided here, the uncorrected points are the default.
     */
    private final Map<Pair<Competitor, TrackedRace>, Integer> correctedScores;
    
    public ScoreCorrectionImpl() {
        this.maxPointsReasons = new HashMap<Util.Pair<Competitor,TrackedRace>, ScoreCorrection.MaxPointsReason>();
        this.correctedScores = new HashMap<Util.Pair<Competitor,TrackedRace>, Integer>();
    }

    @Override
    public void setMaxPointsReason(Competitor competitor, TrackedRace race, MaxPointsReason reason) {
        maxPointsReasons.put(new Pair<Competitor, TrackedRace>(competitor, race), reason);
    }

    @Override
    public void correctScore(Competitor competitor, TrackedRace race, int points) {
        correctedScores.put(new Pair<Competitor, TrackedRace>(competitor, race), points);
    }
    
    @Override
    public boolean isScoreCorrected(Competitor competitor, TrackedRace race) {
        return correctedScores.containsKey(new Pair<Competitor, TrackedRace>(competitor, race));
    }
    
    @Override
    public void uncorrectScore(Competitor competitor, TrackedRace race) {
        correctedScores.remove(new Pair<Competitor, TrackedRace>(competitor, race));
    }

    protected MaxPointsReason getMaxPointsReason(Competitor competitor, TrackedRace trackedRace) {
        MaxPointsReason result = maxPointsReasons.get(new Pair<Competitor, TrackedRace>(competitor, trackedRace));
        if (result == null) {
            result = MaxPointsReason.NONE;
        }
        return result;
    }
    
    /**
     * If the {@link #getMaxPointsReason(Competitor, TrackedRace)} for the <code>competitor</code> for the
     * <code>trackedRace</code> is not {@link MaxPointsReason#NONE}, the {@link #getMaxPoints(TrackedRace) maximum
     * score} is computed for the competitor. Otherwise, the <code>uncorrectedScore</code> is returned.<p>
     */
    @Override
    public Result getCorrectedScore(int uncorrectedScore, Competitor competitor, TrackedRace trackedRace,
            TimePoint timePoint) {
        int result;
        final MaxPointsReason maxPointsReason = getMaxPointsReason(competitor, trackedRace);
        if (maxPointsReason == MaxPointsReason.NONE) {
            result = getCorrectedNonMaxedScore(competitor, trackedRace, uncorrectedScore);
        } else {
            result = getMaxPoints(trackedRace);
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
        };
    }

    /**
     * Under the assumption that the competitor is not assigned the maximum score due to disqualification or other
     * reasons, computes the corrected score. This default implementation uses <code>uncorrectedScore</code> without
     * changes. Subclasses may wish to allow for optionally overwriting this uncorrected score to handle, e.g.,
     * differences between what the tracking results suggest and what the jury or race committee decided.
     */
    protected int getCorrectedNonMaxedScore(Competitor competitor, TrackedRace trackedRace, int uncorrectedScore) {
        Integer correctedNonMaxedScore = correctedScores.get(new Pair<Competitor, TrackedRace>(competitor, trackedRace));
        if (correctedNonMaxedScore == null) {
            return uncorrectedScore;
        } else {
            return correctedNonMaxedScore;
        }
    }

    private int getMaxPoints(TrackedRace trackedRace) {
        return Util.size(trackedRace.getRace().getCompetitors())+1;
    }

}
