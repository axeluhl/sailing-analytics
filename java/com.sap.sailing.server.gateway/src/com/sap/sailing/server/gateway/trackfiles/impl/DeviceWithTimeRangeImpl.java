package com.sap.sailing.server.gateway.trackfiles.impl;

import com.sap.sailing.domain.abstractlog.shared.events.DeviceWithTimeRange;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.TimePoint;

public class DeviceWithTimeRangeImpl implements DeviceWithTimeRange {
    private static final long serialVersionUID = -4676108888517968890L;
    private final DeviceIdentifier device;
    private final TimeRange timeRange;
    
    public DeviceWithTimeRangeImpl(DeviceIdentifier device, TimeRange timeRange) {
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
    public TimePoint getTimePoint() {
        return timeRange.from();
    }
}
