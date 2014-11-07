package com.sap.sailing.domain.abstractlog.race.tracking;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.DeviceMappingFinder;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.WithID;

/**
 * @see DeviceMappingFinder {@code DeviceMappings} are derived from {@code DeviceMappingEvents}. The {@link Timed} interface
 * is to be implemented as providing the {@link TimeRange#from()} time point as the answer to {@link Timed#getTimePoint()}.
 * 
 * @author Fredrik Teschke
 */
public interface DeviceMapping<ItemType extends WithID> extends DeviceWithTimeRange {
    ItemType getMappedTo();
    /**
     * List the ids of those race log events from which this mapping was generated.
     * In the simplest case this is a single event.
     */
    List<Serializable> getOriginalRaceLogEventIds();
}
