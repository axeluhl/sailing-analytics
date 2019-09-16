package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.SavedDevicePositioning;

public class SavedDevicePositioningImpl extends AbstractPositioningImpl implements SavedDevicePositioning {

    public SavedDevicePositioningImpl(Position optionalCurrentPosition) {
        super(PositioningType.Device, optionalCurrentPosition);
    }
}
