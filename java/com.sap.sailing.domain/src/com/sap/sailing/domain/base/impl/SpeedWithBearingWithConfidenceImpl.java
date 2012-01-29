package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.confidence.IsScalable;
import com.sap.sailing.domain.confidence.ScalableValue;

public class SpeedWithBearingWithConfidenceImpl<RelativeTo> extends
        HasConfidenceImpl<Triple<Speed, Double, Double>, SpeedWithBearing, RelativeTo> implements
        SpeedWithBearingWithConfidence<RelativeTo>, IsScalable<Triple<Speed, Double, Double>, SpeedWithBearing> {
    public SpeedWithBearingWithConfidenceImpl(SpeedWithBearing speedWithBearing, double confidence, RelativeTo relativeTo) {
        super(speedWithBearing, confidence, relativeTo);
    }

    /**
     * The scalable value used for averaging confidence-based objects of this type is a triple whose first component
     * holds the speed with a confidence while the second and third element are the sine and cosine values of the bearing's
     * angle.
     */
    @Override
    public ScalableValue<Triple<Speed, Double, Double>, SpeedWithBearing> getScalableValue() {
        return new ScalableSpeedWithBearing(getObject());
    }

    private static class ScalableSpeedWithBearing implements ScalableValue<Triple<Speed, Double, Double>, SpeedWithBearing> {
        private final Speed speed;
        private final double sin;
        private final double cos;
        
        public ScalableSpeedWithBearing(SpeedWithBearing speedWithBearing) {
            this(new KnotSpeedImpl(speedWithBearing.getKnots()), Math.sin(speedWithBearing.getBearing()
                    .getRadians()), Math.cos(speedWithBearing.getBearing().getRadians()));
        }
        
        public ScalableSpeedWithBearing(Speed speed, double sin, double cos) {
            this.speed = speed;
            this.sin = sin;
            this.cos = cos;
        }

        @Override
        public ScalableValue<Triple<Speed, Double, Double>, SpeedWithBearing> multiply(
                double factor) {
            Speed newSpeed = new KnotSpeedImpl(factor*speed.getKnots());
            return new ScalableSpeedWithBearing(newSpeed, factor*sin, factor*cos);
        }

        @Override
        public ScalableValue<Triple<Speed, Double, Double>, SpeedWithBearing> add(
                ScalableValue<Triple<Speed, Double, Double>, SpeedWithBearing> t) {
            Speed newSpeed = new KnotSpeedImpl(speed.getKnots() + t.getValue().getA().getKnots());
            return new ScalableSpeedWithBearing(newSpeed, sin+t.getValue().getB(), cos+t.getValue().getC());
        }

        @Override
        public SpeedWithBearing divide(double divisor, double confidence) {
            Speed newSpeed = new KnotSpeedImpl(speed.getKnots() / divisor);
            double angle;
            if (cos == 0) {
                angle = sin >= 0 ? Math.PI / 2 : -Math.PI / 2;
            } else {
                angle = Math.atan2(sin, cos);
            }
            Bearing bearing = new RadianBearingImpl(angle < 0 ? angle + 2 * Math.PI : angle);
            return new KnotSpeedWithBearingImpl(newSpeed.getKnots(), bearing);
        }

        @Override
        public Triple<Speed, Double, Double> getValue() {
            return new Triple<Speed, Double, Double>(speed, sin, cos);
        }
        
    }
}
