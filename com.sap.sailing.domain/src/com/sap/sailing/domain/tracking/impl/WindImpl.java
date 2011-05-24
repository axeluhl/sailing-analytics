package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.tracking.Wind;

public class WindImpl extends KnotSpeedImpl implements Wind {
    private final Position position;
    private final TimePoint timepoint;

    public WindImpl(Position p, TimePoint at, SpeedWithBearing avgWindSpeed) {
        super(avgWindSpeed.getKnots(), avgWindSpeed.getBearing());
        this.position = p;
        this.timepoint = at;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public TimePoint getTimePoint() {
        return timepoint;
    }

}
