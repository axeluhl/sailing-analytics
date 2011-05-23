package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class GPSFixMovingImpl extends GPSFixImpl implements GPSFixMoving {
    private final SpeedWithBearing speed;
    
    public GPSFixMovingImpl(Position position, TimePoint timePoint, SpeedWithBearing speed) {
        super(position, timePoint);
        this.speed = speed;
    }

    @Override
    public SpeedWithBearing getSpeed() {
        return speed;
    }

}
