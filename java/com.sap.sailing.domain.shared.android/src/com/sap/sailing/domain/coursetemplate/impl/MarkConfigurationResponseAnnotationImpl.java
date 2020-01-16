package com.sap.sailing.domain.coursetemplate.impl;

import java.util.Collections;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.coursetemplate.MarkConfigurationResponseAnnotation;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.TimeRangeImpl;

public class MarkConfigurationResponseAnnotationImpl implements MarkConfigurationResponseAnnotation {
    private final GPSFix lastKnownPosition;
    private final Iterable<Triple<DeviceIdentifier, TimeRange, GPSFix>> deviceMappings;

    public MarkConfigurationResponseAnnotationImpl(GPSFix lastKnownPosition) {
        super();
        this.lastKnownPosition = lastKnownPosition;
        this.deviceMappings = Collections.emptySet();
    }
    
    public MarkConfigurationResponseAnnotationImpl(GPSFix lastKnownPosition, DeviceIdentifier currentTrackingDevice) {
        super();
        this.lastKnownPosition = lastKnownPosition;
        this.deviceMappings = Collections.singleton(new Triple<>(currentTrackingDevice, new TimeRangeImpl(null, null), null));
    }

    public MarkConfigurationResponseAnnotationImpl(GPSFix lastKnownPosition,
            Iterable<Triple<DeviceIdentifier, TimeRange, GPSFix>> deviceMappings) {
        super();
        this.lastKnownPosition = lastKnownPosition;
        this.deviceMappings = deviceMappings;
    }

    @Override
    public GPSFix getLastKnownPosition() {
        return lastKnownPosition;
    }

    @Override
    public Iterable<Triple<DeviceIdentifier, TimeRange, GPSFix>> getDeviceMappings() {
        return deviceMappings;
    }
}
