package com.sap.sailing.domain.racelogtracking;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMappingFinder;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.WithID;

/**
 * @see RegattaLogDeviceMappingFinder {@code DeviceMappings} are derived from {@code DeviceMappingEvents}. The
 *      {@link Timed} interface is to be implemented as providing the {@link TimeRange#from()} time point as the answer
 *      to {@link Timed#getTimePoint()}.
 * 
 *      This instance provides access to the originating event.
 * 
 */
public interface DeviceMappingWithRegattaLogEvent<ItemType extends WithID> extends DeviceMapping<ItemType> {
    
    RegattaLogDeviceMappingEvent<ItemType> getRegattaLogEvent();
}
