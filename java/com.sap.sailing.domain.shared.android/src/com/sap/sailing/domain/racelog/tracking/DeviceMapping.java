package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.WithID;

/**
 * Refer to the documentation of {@link DeviceMappingEvent} for details on how have-open ranges are resolved.
 * @author Fredrik Teschke
 */
public interface DeviceMapping<ItemType extends WithID> extends Timed {
	ItemType getMappedTo();
	DeviceIdentifier getDevice();
	
	TimeRange getTimeRange();
}
