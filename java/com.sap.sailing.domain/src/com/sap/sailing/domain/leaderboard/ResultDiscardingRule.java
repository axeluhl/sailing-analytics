package com.sap.sailing.domain.leaderboard;

import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A result discarding rule is used to determine for a competitor those races from a list of
 * races whose results are to be discarded. Typically, such a rules lists the number of
 * races that may be discarded depending on the total number of races completed so far.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ResultDiscardingRule {
    Set<TrackedRace> getDiscardedRaces(Competitor competitor, Iterable<TrackedRace> races, TimePoint timePoint);
}
