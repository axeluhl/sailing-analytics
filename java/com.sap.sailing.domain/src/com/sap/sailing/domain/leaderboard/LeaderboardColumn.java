package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Named;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;

/**
 * Represents the data common to all columns in a {@link Leaderboard}. A column enables
 * the leaderboard to fetch the total points for a {@link Competitor} as it goes into
 * the score aggregation for the leaderboard. Additionally, the column has a name that
 * the leaderboard may use for display purposes.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface LeaderboardColumn extends Named {
    int getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException;
}
