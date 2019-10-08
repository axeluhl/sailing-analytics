package com.sap.sailing.domain.coursetemplate.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.SmartphoneUUIDPositioning;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;

public class SmartphoneUUIDPositioningImpl extends AbstractPositioningImpl implements SmartphoneUUIDPositioning {

    private final UUID deviceUUID;

    public SmartphoneUUIDPositioningImpl(UUID deviceUUID, Position optionalCurrentPosition) {
        super(PositioningType.DEVICE, optionalCurrentPosition, new SmartphoneUUIDIdentifierImpl(deviceUUID));
        this.deviceUUID = deviceUUID;
    }

    @Override
    public UUID getDeviceUUID() {
        return deviceUUID;
    }
}
