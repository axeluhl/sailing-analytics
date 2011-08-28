package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.AbstractBearing;
import com.sap.sailing.domain.base.impl.AbstractPosition;
import com.sap.sailing.domain.base.impl.AbstractSpeedImpl;
import com.sap.sailing.domain.base.impl.AbstractTimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;

/**
 * A memory-conserving representation of a {@link GPSFixMoving} object that produces the fine-grained
 * objects for {@link Position}, {@link SpeedWithBearing}, {@link Bearing} and {@link TimePoint} dynamically
 * as thin wrappers around this object which holds all elementary attributes required. This saves several
 * object references and object headers.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompactGPSFixMovingImpl implements GPSFixMoving {
    private final double latDeg;
    private final double lngDeg;
    private final long timePointAsMillis;
    private final double knotSpeed;
    private final double degBearing;
    
    private class CompactSpeedWithBearing extends AbstractSpeedImpl implements SpeedWithBearing {
        @Override
        public double getKnots() {
            return knotSpeed;
        }

        @Override
        public Bearing getBearing() {
            return new CompactBearing();
        }

        @Override
        public Position travelTo(Position pos, TimePoint from, TimePoint to) {
            return pos.translateGreatCircle(getBearing(), this.travel(from, to));
        }
        
        @Override
        public String toString() {
            return super.toString()+" to "+getBearing().getDegrees()+"°";
        }
    }
    
    private class CompactBearing extends AbstractBearing {
        @Override
        public double getDegrees() {
            return degBearing;
        }

        @Override
        public double getRadians() {
            return degBearing / 180. * Math.PI;
        }
    }
    
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
    
    public CompactGPSFixMovingImpl(Position position, TimePoint timePoint, SpeedWithBearing speed) {
        latDeg = position.getLatDeg();
        lngDeg = position.getLngDeg();
        timePointAsMillis = timePoint.asMillis();
        knotSpeed = speed.getKnots();
        degBearing = speed.getBearing().getDegrees();
    }
    
    public CompactGPSFixMovingImpl(GPSFixMoving gpsFixMoving) {
        this(gpsFixMoving.getPosition(), gpsFixMoving.getTimePoint(), gpsFixMoving.getSpeed());
    }

    @Override
    public SpeedWithBearing getSpeed() {
        return new CompactSpeedWithBearing();
    }

    @Override
    public String toString() {
        return getTimePoint() + ": " + getPosition() + " with " + getSpeed();
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
