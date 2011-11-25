package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.AbstractPosition;
import com.sap.sailing.domain.base.impl.AbstractTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;

public class CompactGPSFixImpl extends AbstractGPSFixImpl implements GPSFix {
    private final double latDeg;
    private final double lngDeg;
    private final long timePointAsMillis;
    
    private class CompactPosition extends AbstractPosition {
        @Override
        public double getLatDeg() {
            return latDeg;
        }

        @Override
        public double getLngDeg() {
            return lngDeg;
        }
    }
    
    private class CompactTimePoint extends AbstractTimePoint implements TimePoint {
        @Override
        public long asMillis() {
            return timePointAsMillis;
        }
    }
    
    public CompactGPSFixImpl(Position position, TimePoint timePoint) {
        latDeg = position.getLatDeg();
        lngDeg = position.getLngDeg();
        timePointAsMillis = timePoint.asMillis();
    }
    
    public CompactGPSFixImpl(GPSFix gpsFix) {
        this(gpsFix.getPosition(), gpsFix.getTimePoint());
    }

    @Override
    public String toString() {
        return getTimePoint() + ": " + getPosition();
    }

    @Override
    public Position getPosition() {
        return new CompactPosition();
    }

    @Override
    public TimePoint getTimePoint() {
        return new CompactTimePoint();
    }

}
