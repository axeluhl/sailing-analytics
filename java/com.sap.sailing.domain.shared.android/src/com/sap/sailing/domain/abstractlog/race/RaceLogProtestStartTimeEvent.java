package com.sap.sailing.domain.abstractlog.race;

import com.sap.sse.common.TimeRange;

/**
 * Indicates that the protest start time for the related race has changed.
 *
 */
public interface RaceLogProtestStartTimeEvent extends RaceLogEvent {

    /**
     * Returns the time range of the race's protest time. The result is never {@code null}
     * and the {@link TimeRange} returned always has a valid {@link TimeRange#from() start}
     * and {@link TimeRange#to() end}.
     */
    TimeRange getProtestTime();
}
