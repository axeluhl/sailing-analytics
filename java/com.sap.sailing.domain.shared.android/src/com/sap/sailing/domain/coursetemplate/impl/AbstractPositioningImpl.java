package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.Positioning;

public abstract class AbstractPositioningImpl implements Positioning {
    
    private final PositioningType type;
    private final Position position;
    private final DeviceIdentifier deviceIdentifier;

    public AbstractPositioningImpl(PositioningType type, Position position, DeviceIdentifier deviceIdentifier) {
        this.type = type;
        this.position = position;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public PositioningType getType() {
        return type;
    }

    @Override
    public Position getPosition() {
        return position;
    }
    
    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }
}
