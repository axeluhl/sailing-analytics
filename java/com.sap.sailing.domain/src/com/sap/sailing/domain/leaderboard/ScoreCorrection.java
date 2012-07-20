package com.sap.sailing.domain.leaderboard;

import java.io.Serializable;
import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Manages score corrections for a competitor in a race, in particular handling the following use cases:
 * <ul>
 * <li>competitor disqualified: maximum points will be granted to the competitor for that race</li>
 * <li>imprecise tracking for finish line: jury changed final rankings; usually several competitors affected</li>
 * </ul>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface ScoreCorrection extends Serializable {
    public interface Result {
        double getCorrectedScore();
        MaxPointsReason getMaxPointsReason();
        boolean isCorrected();
    }
    
    /**
     * Returns the effective score for the <code>competitor</code> scored in race <code>trackedRace</code>. If no
     * explicit correction has been recorded in this score correction object, the uncorrected score will be returned,
     * and {@link MaxPointsReason#NONE} will be listed as the {@link Result#getMaxPointsReason() correction reason}.
     * Note, though, that {@link MaxPointsReason#NONE} can also be the reason for an explicit score correction, e.g., if
     * the tracking results were overruled by the jury. Clients may use
     * {@link #isScoreCorrected(Competitor, TrackedRace)} to detect the difference.
     * 
     * @param numberOfCompetitors
     *            the number of competitors to use as the basis for penalty score calculation ("max points")
     */
    Result getCorrectedScore(Callable<Integer> uncorrectedScore, Competitor competitor, RaceColumn raceColumn,
            TimePoint timePoint, int numberOfCompetitors);

    /**
     * Note the difference between what this method does and a more naive comparison of uncorrected and corrected score.
     * Should, for some reason, the uncorrected score change later, an existing score correction would still remain in
     * place whereas if no score correction exists for the competitor/race combination, the resulting score after
     * "correction" will still be the uncorrected value.<p>
     * 
     * @return if an explicit score correction was made for the combination of <code>competitor</code> and
     *         <code>raceColumn</code>
     */
    boolean isScoreCorrected(Competitor competitor, RaceColumn raceColumn);

    /**
     * Tells when the score correction was last updated. This should usually be the "validity time" and not the
     * "transaction time." In other words, if scores provided by the race committee are updated to this score correction
     * at time X, and the race committee's scores are tagged with time Y, then this method should return Y, not X. If
     * Y is not available for some reason, X may be used as a default.
     */
    TimePoint getTimePointOfLastCorrectionsValidity();
    
    /**
     * A free-form comment to display to the viewers of the leaderboard that has these score corrections. It should make
     * crystal clear if the scores are preliminary or not yet jury-finalized. If <code>null</code> is returned, this
     * always has to be interpreted as "preliminary" because then no comment as to the correctness have been made.
     */
    String getComment();
}
