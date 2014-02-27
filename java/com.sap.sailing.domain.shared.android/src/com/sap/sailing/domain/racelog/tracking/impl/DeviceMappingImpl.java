package com.sap.sailing.domain.racelog.tracking.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.DeviceMapping;

public class DeviceMappingImpl<ItemType extends WithID> implements DeviceMapping<ItemType> {
    private static final long serialVersionUID = -4602948402371623654L;

    private final ItemType mappedTo;
    private final DeviceIdentifier device;
    private final TimeRange timeRange;

    public DeviceMappingImpl(ItemType mappedTo, DeviceIdentifier device, TimeRange timeRange) {
        this.mappedTo = mappedTo;
        this.device = device;
        this.timeRange = timeRange;
    }

    @Override
    public DeviceIdentifier getDevice() {
        return device;
    }

    @Override
    public TimeRange getTimeRange() {
        return timeRange;
    }

    @Override
    public ItemType getMappedTo() {
        return mappedTo;
    }

    @Override
    public TimePoint getTimePoint() {
        return timeRange.from();
    }
}
