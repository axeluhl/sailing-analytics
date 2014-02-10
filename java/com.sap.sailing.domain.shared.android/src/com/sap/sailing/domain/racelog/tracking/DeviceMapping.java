package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.WithID;

public interface DeviceMapping<ItemType extends WithID> extends Timed {
	ItemType getMappedTo();
	DeviceIdentifier getDevice();
	TimeRange getTimeRange();
}
