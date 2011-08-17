package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
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
public interface ScoreCorrection {
    /**
     * The reasons why a competitor may get the maximum number of points, usually equaling the
     * number of competitors enlisted for the regatta plus one.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    enum MaxPointsReason {
        /** The competitor finished the race properly */
        NONE,
        /** Did Not Start */
        DNS, 
        /** Did Not Finish */
        DNF,
        /** DiSQualified */
        DSQ,
        /** On Course Side (jumped the gun) */
        OCS,
        /** Disqualified, non-discardable */
        DND,
        /** Black Flag Disqualified */
        BFD,
        /** Did Not Compete */
        DNC,
        /** Retired After Finishing */
        RAF
    };
    
    public interface Result {
        int getCorrectedScore();
        MaxPointsReason getMaxPointsReason();
    }
    
    /**
     * Returns the effective score for the <code>competitor</code> scored in race <code>trackedRace</code>. If no
     * explicit correction has been recorded in this score correction object, the uncorrected score will be returned,
     * and {@link MaxPointsReason#NONE} will be listed as the {@link Result#getMaxPointsReason() correction reason}.
     * Note, though, that {@link MaxPointsReason#NONE} can also be the reason for an explicit score correction, e.g., if
     * the tracking results were overruled by the jury. Clients may use
     * {@link #isScoreCorrected(Competitor, TrackedRace)} to detect the difference.
     */
    Result getCorrectedScore(int uncorrectedScore, Competitor competitor, RaceInLeaderboard trackedRace, TimePoint timePoint);

    /**
     * Note the difference between what this method does and a more naive comparison of uncorrected and corrected score.
     * Should, for some reason, the uncorrected score change later, an existing score correction would still remain in
     * place whereas if no score correction exists for the competitor/race combination, the resulting score after
     * "correction" will still be the uncorrected value.
     * 
     * @return if an explicit score correction was made for the combination of <code>competitor</code> and <code>race</code>
     */
    boolean isScoreCorrected(Competitor competitor, TrackedRace race);

}
