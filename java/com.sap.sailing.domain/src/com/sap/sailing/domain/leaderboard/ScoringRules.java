package com.sap.sailing.domain.leaderboard;

import java.math.BigDecimal;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Tells how ranks and disqualifications of a {@link TrackedRace} are converted into points in the scope
 * of a {@link Regatta}, also considering rules for discarding results. Different implementations may, e.g.,
 * distinguish between low-point and high-point scoring schemes, schemes where a winner's score is further
 * improved as well as different discarding thresholds.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ScoringRules {
    /**
     * Note that scores can be fractional numbers, e.g., if the scoring rule determines that the winner's rank
     * is divided by two to obtain their score.
     */
    BigDecimal getScore(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint);
}
