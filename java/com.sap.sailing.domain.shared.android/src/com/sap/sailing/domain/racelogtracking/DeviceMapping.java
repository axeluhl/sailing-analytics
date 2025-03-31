package com.sap.sailing.domain.racelogtracking;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMappingFinder;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.WithID;

/**
 * @see RegattaLogDeviceMappingFinder {@code DeviceMappings} are derived from {@code DeviceMappingEvents}. The {@link Timed} interface
 * is to be implemented as providing the {@link TimeRange#from()} time point as the answer to {@link Timed#getTimePoint()}.
 * 
 * @author Fredrik Teschke
 */
public interface DeviceMapping<ItemType extends WithID> extends Timed {
    ItemType getMappedTo();
    /**
     * List the ids of those race log events from which this mapping was generated.
     * In the simplest case this is a single event.
     */
    List<Serializable> getOriginalRaceLogEventIds();
    
    DeviceIdentifier getDevice();

    TimeRange getTimeRange();
    
    Class<?> getEventType();
    
}
