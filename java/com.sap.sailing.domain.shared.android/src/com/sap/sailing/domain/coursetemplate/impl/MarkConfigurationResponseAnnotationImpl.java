package com.sap.sailing.domain.coursetemplate.impl;

import java.util.Collections;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.MarkConfigurationResponseAnnotation;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.TimeRangeImpl;

public class MarkConfigurationResponseAnnotationImpl implements MarkConfigurationResponseAnnotation {
    private final Position lastKnownPosition;
    private final Iterable<Pair<DeviceIdentifier, TimeRange>> deviceMappings;

    public MarkConfigurationResponseAnnotationImpl(Position lastKnownPosition) {
        super();
        this.lastKnownPosition = lastKnownPosition;
        this.deviceMappings = Collections.emptySet();
    }
    
    public MarkConfigurationResponseAnnotationImpl(Position lastKnownPosition, DeviceIdentifier currentTrackingDevice) {
        super();
        this.lastKnownPosition = lastKnownPosition;
        this.deviceMappings = Collections.singleton(new Pair<>(currentTrackingDevice, new TimeRangeImpl(null, null)));
    }

    public MarkConfigurationResponseAnnotationImpl(Position lastKnownPosition,
            Iterable<Pair<DeviceIdentifier, TimeRange>> deviceMappings) {
        super();
        this.lastKnownPosition = lastKnownPosition;
        this.deviceMappings = deviceMappings;
    }

    @Override
    public Position getLastKnownPosition() {
        return lastKnownPosition;
    }

    @Override
    public Iterable<Pair<DeviceIdentifier, TimeRange>> getDeviceMappings() {
        return deviceMappings;
    }
}
