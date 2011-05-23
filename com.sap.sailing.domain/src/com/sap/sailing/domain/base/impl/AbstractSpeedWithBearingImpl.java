package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;

public abstract class AbstractSpeedWithBearingImpl extends AbstractSpeedImpl implements SpeedWithBearing {
    private final Bearing bearing;
    
    protected AbstractSpeedWithBearingImpl(Bearing bearing) {
        this.bearing = bearing;
    }

    @Override
    public Bearing getBearing() {
        return bearing;
    }

    @Override
    public Position travelTo(Position pos, TimePoint from, TimePoint to) {
        return pos.translateGreatCircle(getBearing(), this.travel(from, to));
    }

}
