package com.sap.sailing.domain.leaderboard;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;

/**
 * A regatta leaderboard that is derived from another regatta leaderboard by eliminating a subset of the competitors and
 * that provides its own, unique name and optionally its own display name. This generally implements a "delegate"
 * pattern for a {@link RegattaLeaderboard}. It therefore does not maintain its own score corrections or set of
 * suppressed competitors. Note: "suppressed" is different from "eliminated" in that suppressed competitors do not show
 * in any race and are not assigned any rank in any race, but eliminated competitors are; they only don't receive a
 * regatta ("total") rank, and all competitors advance by as many ranks compared to the original leaderboard as there
 * are eliminated competitors ranking better in the original leaderboard.
 * 
 * @author Axel Uhl (d043530)
 */
public interface RegattaLeaderboardWithEliminations extends RegattaLeaderboard {

    void setEliminated(Competitor competitor, boolean eliminated);

    boolean isEliminated(Competitor competitor);
    
    default Set<Competitor> getEliminatedCompetitors() {
        final Set<Competitor> result = new HashSet<>();
        for (final Competitor c : getAllCompetitors()) {
            if (isEliminated(c)) {
                result.add(c);
            }
        }
        return result;
    }

}
