package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.CourseChange;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.AbstractBearing;
import com.sap.sailing.domain.base.impl.AbstractSpeedImpl;
import com.sap.sailing.domain.base.impl.AbstractSpeedWithBearingImpl;
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
public class CompactGPSFixMovingImpl extends CompactGPSFixImpl implements GPSFixMoving {
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
        public SpeedWithBearing applyCourseChange(CourseChange courseChange) {
            return AbstractSpeedWithBearingImpl.applyCourseChange(this, courseChange);
        }

        @Override
        public CourseChange getCourseChangeRequiredToReach(SpeedWithBearing targetSpeedWithBearing) {
            return AbstractSpeedWithBearingImpl.getCourseChangeRequiredToReach(getSpeed(), targetSpeedWithBearing);
        }

        @Override
        public String toString() {
            return super.toString()+" to "+getBearing().getDegrees()+"°";
        }
        @Override
        public int hashCode() {
            return super.hashCode() ^ getBearing().hashCode();
        }
        
        @Override
        public boolean equals(Object object) {
            return super.equals(object) && object instanceof SpeedWithBearing
                    && getBearing().equals(((SpeedWithBearing) object).getBearing());
        }
    }
    
    private class CompactBearing extends AbstractBearing {
        @Override
        public double getConfidence() {
            return DEFAULT_BEARING_CONFIDENCE;
        }

        @Override
        public double getDegrees() {
            return degBearing;
        }

        @Override
        public double getRadians() {
            return degBearing / 180. * Math.PI;
        }
    }
    
    public CompactGPSFixMovingImpl(Position position, TimePoint timePoint, SpeedWithBearing speed) {
        super(position, timePoint);
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
        return super.toString() + " with " + getSpeed();
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ getSpeed().hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return super.equals(other) && other instanceof GPSFixMoving && getSpeed().equals(((GPSFixMoving) other).getSpeed());
    }
}
