package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.Timed;

/**
 * Mark passings have an {@link #equals(Object)} and {@link #hashCode()} implementation based
 * on their content, not their identity.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MarkPassing extends Timed {
    Waypoint getWaypoint();

    Competitor getCompetitor();

    /**
     * This mark passing may be a replacement of another mark passing that was created based on some other evidence,
     * such as a race committee-provided finishing time. In this case, this method will return the original mark
     * passing that was replaced. For regular mark passings, this method will return {@code this} object. It may
     * return {@code null} if this mark passing has been synthesized, e.g., solely from a {@link RaceLog} event and needs
     * to be removed again if that event is removed because it didn't replace any original mark passing.
     * 
     * TODO what if we receive a real update for the finish mark passing?
     */
    MarkPassing getOriginal();
}
