package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.StoredDeviceIdentifierPositioning;

public class StoredDeviceIdentifierPositioningImpl extends AbstractPositioningImpl implements StoredDeviceIdentifierPositioning {

    public StoredDeviceIdentifierPositioningImpl(Position optionalCurrentPosition) {
        super(PositioningType.DEVICE, optionalCurrentPosition);
    }
}
