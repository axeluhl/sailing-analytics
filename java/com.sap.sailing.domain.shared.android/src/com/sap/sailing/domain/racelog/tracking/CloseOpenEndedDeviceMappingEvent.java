package com.sap.sailing.domain.racelog.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.Revokable;

/**
 * Closes the time range of a {@link DeviceMappingEvent}. This means that the {@link #getClosingTimePoint() timepoint} provided
 * by this event is substituted for the missing timepoint in the {@code DeviceMappingEvent} it refers to.
 * @author Fredrik Teschke
 *
 */
public interface CloseOpenEndedDeviceMappingEvent extends RaceLogEvent, Revokable {
    /**
     * Returns the timepoint that shall substitute the missing one in the corresponding {@link DeviceMappingEvent}.
     */
    TimePoint getClosingTimePoint();
    
    /**
     * Returns the {@code id} of the event for which this event provides the closing timepoint.
     * @return
     */
    Serializable getDeviceMappingEventId();
}
