package com.sap.sailing.domain.coursetemplate.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.SmartphoneUUIDPositioning;

public class SmartphoneUUIDPositioningImpl extends AbstractPositioningImpl implements SmartphoneUUIDPositioning {

    private final UUID deviceUUID;

    public SmartphoneUUIDPositioningImpl(UUID deviceUUID, Position optionalCurrentPosition) {
        super(PositioningType.Device, optionalCurrentPosition);
        this.deviceUUID = deviceUUID;
    }

    @Override
    public UUID getDeviceUUID() {
        return deviceUUID;
    }
}
