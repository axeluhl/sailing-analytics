package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.impl.AbstractSimpleLeaderboardImpl;
import com.sap.sse.common.TimePoint;

/**
 * Computing the competitors can be a bit expensive, particularly if the fleet is large and there may be suppressed
 * competitors, and the leaderboard may be a meta-leaderboard that refers to other leaderboards which each have
 * several tracked races attached from where the competitors need to be retrieved. Ideally, the competitors list
 * would be cached, but that is again difficult because we would have to monitor all changes in all dependent
 * leaderboards and columns and tracked races properly.
 * <p>
 * 
 * As it turns out, one of the most frequent uses of the {@link AbstractSimpleLeaderboardImpl#getCompetitors}
 * competitors list is to determine their number which in turn is only required for high-point scoring systems and
 * for computing the default score for penalties. Again, the most frequently used low-point family of scoring schemes
 * does not require this number. Yet, the scoring scheme requires an argument for polymorphic use by those that
 * need it. Instead of computing it for each call, this interface lets us defer the actual calculation until the
 * point when it's really needed. Once asked, this object will cache the result. Therefore, a new one should be
 * constructed each time the number shall be computed.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface NumberOfCompetitorsInLeaderboardFetcher {
    int getNumberOfCompetitorsInLeaderboard();
    int getNumberOfCompetitorsWithoutMaxPointReason(RaceColumn column, TimePoint timePoint);
}
