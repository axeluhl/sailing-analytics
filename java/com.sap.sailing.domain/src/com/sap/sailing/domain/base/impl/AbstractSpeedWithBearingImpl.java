package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.BearingWithConfidence;
import com.sap.sailing.domain.base.CourseChange;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.common.Util.Triple;
import com.sap.sailing.domain.confidence.ScalableValue;

public abstract class AbstractSpeedWithBearingImpl extends AbstractSpeedImpl implements SpeedWithBearingWithConfidence {
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

    @Override
    public CourseChange getCourseChangeRequiredToReach(SpeedWithBearing targetSpeedWithBearing) {
        return AbstractSpeedWithBearingImpl.getCourseChangeRequiredToReach(this, targetSpeedWithBearing);
    }

    public static CourseChange getCourseChangeRequiredToReach(SpeedWithBearing from, SpeedWithBearing to) {
        double courseChangeInDegrees = to.getBearing().getDegrees() - from.getBearing().getDegrees();
        if (courseChangeInDegrees < -180.) {
            courseChangeInDegrees += 360.;
        } else if (courseChangeInDegrees > 180.) {
            courseChangeInDegrees -= 360.;
        }
        double speedChangeInKnots = to.getKnots() - from.getKnots();
        return new CourseChangeImpl(courseChangeInDegrees, speedChangeInKnots);
    }
    
    @Override
    public SpeedWithBearing applyCourseChange(CourseChange courseChange) {
        return applyCourseChange(this, courseChange);
    }
    
    public static SpeedWithBearing applyCourseChange(SpeedWithBearing from, CourseChange courseChange) {
        double newBearingDeg = from.getBearing().getDegrees() + courseChange.getCourseChangeInDegrees();
        if (newBearingDeg < 0) {
            newBearingDeg += 360;
        } else if (newBearingDeg > 360) {
            newBearingDeg -= 360;
        }
        Bearing newBearing = new DegreeBearingImpl(newBearingDeg);
        double newSpeedInKnots = from.getKnots()+courseChange.getSpeedChangeInKnots();
        return new KnotSpeedWithBearingImpl(newSpeedInKnots, newBearing);
    }
    
    @Override
    public double getConfidence() {
        return AbstractSpeedWithConfidence.DEFAULT_SPEED_CONFIDENCE;
    }

    /**
     * The scalable value used for averaging confidence-based objects of this type is a triple whose first component
     * holds the speed with a confidence while the second and third element are the sine and cosine values of the bearing's
     * angle.
     */
    @Override
    public ScalableValue<Triple<SpeedWithConfidence, Double, Double>, SpeedWithBearingWithConfidence> getScalableValue() {
        return new ScalableSpeedWithBearing(new KnotSpeedImpl(getKnots()), Math.sin(getBearing().getRadians()), Math.cos(getBearing().getRadians()));
    }

    private static class ScalableSpeedWithBearing implements ScalableValue<Triple<SpeedWithConfidence, Double, Double>, SpeedWithBearingWithConfidence> {
        private final SpeedWithConfidence speedWithConfidence;
        private final double sin;
        private final double cos;
        
        public ScalableSpeedWithBearing(SpeedWithConfidence speedWithConfidence, double sin, double cos) {
            this.speedWithConfidence = speedWithConfidence;
            this.sin = sin;
            this.cos = cos;
        }

        @Override
        public ScalableValue<Triple<SpeedWithConfidence, Double, Double>, SpeedWithBearingWithConfidence> multiply(
                double factor) {
            SpeedWithConfidence newSpeedWithConfidence = speedWithConfidence.getScalableValue().multiply(factor).getValue();
            return new ScalableSpeedWithBearing(newSpeedWithConfidence, factor*sin, factor*cos);
        }

        @Override
        public ScalableValue<Triple<SpeedWithConfidence, Double, Double>, SpeedWithBearingWithConfidence> add(
                ScalableValue<Triple<SpeedWithConfidence, Double, Double>, SpeedWithBearingWithConfidence> t) {
            SpeedWithConfidence newSpeedWithConfidence = speedWithConfidence.getScalableValue().add(t.getValue().getA().getScalableValue()).getValue();
            return new ScalableSpeedWithBearing(newSpeedWithConfidence, sin+t.getValue().getB(), cos+t.getValue().getC());
        }

        @Override
        public SpeedWithBearingWithConfidence divide(double divisor) {
            SpeedWithConfidence newSpeedWithConfidence = speedWithConfidence.getScalableValue().divide(divisor);
            double angle;
            if (cos == 0) {
                angle = sin >= 0 ? Math.PI / 2 : -Math.PI / 2;
            } else {
                angle = Math.atan2(sin, cos);
            }
            BearingWithConfidence bearing = new RadianBearingImpl(angle < 0 ? angle + 2 * Math.PI : angle);
            return new KnotSpeedWithBearingImpl(newSpeedWithConfidence.getKnots(), bearing);
        }

        @Override
        public Triple<SpeedWithConfidence, Double, Double> getValue() {
            return new Triple<SpeedWithConfidence, Double, Double>(speedWithConfidence, sin, cos);
        }
        
    }
}
