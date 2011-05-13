package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class GPSFixMovingImpl extends GPSFixImpl implements GPSFixMoving {
    private final Speed speed;
    
    public GPSFixMovingImpl(Position position, TimePoint timePoint, Speed speed) {
        super(position, timePoint);
        this.speed = speed;
    }

    @Override
    public Speed getSpeed() {
        return speed;
    }

}
