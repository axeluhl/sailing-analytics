package com.sap.sailing.domain.abstractlog.race;

import com.sap.sse.common.TimeRange;

/**
 * Indicates that the protest start time for the related race has changed.
 *
 */
public interface RaceLogProtestStartTimeEvent extends RaceLogEvent {

    /**
     * Returns the time range of the race's protest time.
     */
    TimeRange getProtestTime();
}
