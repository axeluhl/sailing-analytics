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
        DSQ
    };
    
    public interface Result {
        int getCorrectedScore();
        MaxPointsReason getMaxPointsReason();
    }
    
    Result getCorrectedScore(int uncorrectedScore, Competitor competitor, TrackedRace trackedRace, TimePoint timePoint);
}
