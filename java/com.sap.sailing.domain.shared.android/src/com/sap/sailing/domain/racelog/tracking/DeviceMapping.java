package com.sap.sailing.domain.racelog.tracking;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.racelog.tracking.analyzing.impl.DeviceMappingFinder;

/**
 * @see DeviceMappingFinder {@code DeviceMappings} are derived from {@code DeviceMappingEvents}.
 * @author Fredrik Teschke
 */
public interface DeviceMapping<ItemType extends WithID> extends Timed {
    ItemType getMappedTo();
    DeviceIdentifier getDevice();

    TimeRange getTimeRange();
    
    /**
     * List the ids of those race log events from which this mapping was generated.
     * In the simplest case this is a single event.
     */
    List<Serializable> getOriginalRaceLogEventIds();
}
