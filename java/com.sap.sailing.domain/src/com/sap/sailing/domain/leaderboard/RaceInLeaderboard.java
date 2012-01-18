package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A column in a {@link Leaderboard} that represents the data of a race. Over the life time of this object it can be
 * assigned a {@link TrackedRace} which then acts as a data provider to this column. If no tracked race has been
 * assigned, the scores reported by this column will all default to zero.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface RaceInLeaderboard extends LeaderboardColumn {
    void setTrackedRace(TrackedRace race);

    TrackedRace getTrackedRace();
    
    /**
     * A "medal race" cannot be discarded. It's score is doubled during score aggregation.
     */
    boolean isMedalRace();
    
    void setIsMedalRace(boolean isMedalRace);
    
    void setName(String newName);
    
    /**
     * Constructs a key for maps storing corrections such as score corrections and max points reasons.
     */
    Pair<Competitor, RaceInLeaderboard> getKey(Competitor competitor);
    
    
}
