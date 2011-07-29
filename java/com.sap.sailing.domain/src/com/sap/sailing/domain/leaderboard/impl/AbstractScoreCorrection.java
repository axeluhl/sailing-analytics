package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.leaderboard.ScoreCorrection;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.Util;

/**
 * Implements the basic logic of assigning a maximum score to a competitor in a race if that competitor was
 * disqualified, did not start or did not finish. The maximum score is determined by counting the number of competitors
 * listed in the event to which the race belongs.
 * <p>
 * 
 * Subclasses should consider redefining {@link #getMaxPointsReason(Competitor, TrackedRace)} and
 * {@link #getCorrectedNonMaxedScore(Competitor, TrackedRace, int)} if they want to manage, e.g., disqualifications and
 * jury overruling of tracking results.
 * <p>
 * 
 * To allow tests to use a trivial implementation of the {@link ScoreCorrection} interface, this class which carries
 * "Abstract" in its name is non-abstract.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class AbstractScoreCorrection implements ScoreCorrection {
    
    protected MaxPointsReason getMaxPointsReason(Competitor competitor, TrackedRace trackedRace) {
        return MaxPointsReason.NONE;
    }
    
    /**
     * If the {@link #getMaxPointsReason(Competitor, TrackedRace)} for the <code>competitor</code> for the
     * <code>trackedRace</code> is not {@link MaxPointsReason#NONE}, the {@link #getMaxPoints(TrackedRace) maximum
     * score} is computed for the competitor. Otherwise, the <code>uncorrectedScore</code> is returned.<p>
     */
    @Override
    public int getCorrectedScore(int uncorrectedScore, Competitor competitor, TrackedRace trackedRace,
            TimePoint timePoint) {
        int result;
        if (getMaxPointsReason(competitor, trackedRace) == MaxPointsReason.NONE) {
            result = getCorrectedNonMaxedScore(competitor, trackedRace, uncorrectedScore);
        } else {
            result = getMaxPoints(trackedRace);
        }
        return getCorrectedNonMaxedScore(competitor, trackedRace, result);
    }

    /**
     * Under the assumption that the competitor is not assigned the maximum score due to disqualification or other
     * reasons, computes the corrected score. This default implementation uses <code>uncorrectedScore</code> without
     * changes. Subclasses may wish to allow for optionally overwriting this uncorrected score to handle, e.g.,
     * differences between what the tracking results suggest and what the jury or race committee decided.
     */
    protected int getCorrectedNonMaxedScore(Competitor competitor, TrackedRace trackedRace, int uncorrectedScore) {
        return uncorrectedScore;
    }

    private int getMaxPoints(TrackedRace trackedRace) {
        return Util.size(trackedRace.getRace().getCompetitors())+1;
    }

}
