package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.GPSFix;

public class GPSFixImpl implements GPSFix {
    private final Position position;
    private final TimePoint timePoint;
    
    public GPSFixImpl(Position position, TimePoint timePoint) {
        super();
        this.position = position;
        this.timePoint = timePoint;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
    }

}
