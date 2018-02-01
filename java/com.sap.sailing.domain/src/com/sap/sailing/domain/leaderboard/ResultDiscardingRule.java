package com.sap.sailing.domain.leaderboard;

import java.io.Serializable;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sse.common.TimePoint;

/**
 * A result discarding rule is used to determine for a competitor those races from a list of
 * races whose results are to be discarded. Typically, such a rules lists the number of
 * races that may be discarded depending on the total number of races completed so far.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ResultDiscardingRule extends Serializable {
    /**
     * @param raceColumnsToConsider if a column is <code>not</code> contained, its existence will be ignored for determining the
     * columns to discard. It affects the count of races. Only columns contained can be part of the result.
     */
    Set<RaceColumn> getDiscardedRaceColumns(Competitor competitor, Leaderboard leaderboard,
            Iterable<RaceColumn> raceColumnsToConsider, TimePoint timePoint);
}
