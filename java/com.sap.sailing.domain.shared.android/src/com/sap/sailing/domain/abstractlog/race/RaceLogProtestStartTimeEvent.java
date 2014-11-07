package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.common.TimePoint;

/**
 * Indicates that the protest start time for the related race has changed.
 *
 */
public interface RaceLogProtestStartTimeEvent extends RaceLogEvent {

    /**
     * Returns the start time of the race's protest time.
     */
    TimePoint getProtestStartTime();
}
