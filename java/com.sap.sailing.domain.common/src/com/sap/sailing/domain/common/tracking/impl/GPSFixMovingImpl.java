package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class GPSFixMovingImpl extends GPSFixImpl implements GPSFixMoving {
    private static final long serialVersionUID = 6508021498142383100L;
    private final SpeedWithBearing speed;
    
    public GPSFixMovingImpl(Position position, TimePoint timePoint, SpeedWithBearing speed) {
        super(position, timePoint);
        this.speed = speed;
    }

    @Override
    public SpeedWithBearing getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return super.toString()+" with "+getSpeed();
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ getSpeed().hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return super.equals(other) && other instanceof GPSFixMoving && getSpeed().equals(((GPSFixMoving) other).getSpeed());
    }
    
    public static GPSFixMovingImpl create(double lonDeg, double latDeg, long timeMillis,
            double speedInKnots, double bearingDeg) {
        return new GPSFixMovingImpl(new DegreePosition(latDeg, lonDeg),
                new MillisecondsTimePoint(timeMillis), new KnotSpeedWithBearingImpl(
                        speedInKnots, new DegreeBearingImpl(bearingDeg)));
    }
}
