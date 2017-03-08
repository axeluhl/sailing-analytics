package com.sap.sailing.domain.abstractlog.regatta.events;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sse.common.TimePoint;

/**
 * Closes the time range of a {@link RegattaLogDeviceMappingEvent}. This means that the {@link #getClosingTimePointInclusive() timepoint} provided
 * by this event is substituted for the missing timepoint in the {@code DeviceMappingEvent} it refers to.
 * @author Fredrik Teschke
 *
 */
public interface RegattaLogCloseOpenEndedDeviceMappingEvent extends RegattaLogEvent, Revokable {
    /**
     * Returns the timepoint that shall substitute the missing one in the corresponding {@link RegattaLogDeviceMappingEvent}.
     * As with {@link RegattaLogDeviceMappingEvent#getToInclusive()}, this time point marks the <em>inclusive</em> end
     * of the interval. 
     */
    TimePoint getClosingTimePointInclusive();
    
    /**
     * Returns the {@code id} of the event for which this event provides the closing timepoint.
     * @return
     */
    Serializable getDeviceMappingEventId();
}
