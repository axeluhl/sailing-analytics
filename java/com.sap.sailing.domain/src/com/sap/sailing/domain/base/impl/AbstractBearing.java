package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.BearingWithConfidence;
import com.sap.sailing.domain.common.Util.Pair;
import com.sap.sailing.domain.confidence.ScalableValue;

public abstract class AbstractBearing implements BearingWithConfidence {
    protected static final double DEFAULT_BEARING_CONFIDENCE = 0.9;

    @Override
    public Bearing reverse() {
        if (getDegrees() >= 180) {
            return new DegreeBearingImpl(getDegrees()-180);
        } else {
            return new DegreeBearingImpl(getDegrees()+180);
        }
    }
    
    @Override
    public Bearing add(Bearing diff) {
        double newDeg = getDegrees() + diff.getDegrees();
        if (newDeg > 360) {
            newDeg -= 360;
        } else if (newDeg < 0) {
            newDeg += 360;
        }
        return new DegreeBearingImpl(newDeg);
    }

    @Override
    public Bearing getDifferenceTo(Bearing b) {
        double diff = b.getDegrees() - getDegrees();
        if (diff < -180) {
            diff += 360;
        } else if (diff > 180) {
            diff -= 360;
        }
        return new DegreeBearingImpl(diff);
    }

    @Override
    public Bearing middle(Bearing other) {
        Bearing result = new DegreeBearingImpl((getDegrees() + other.getDegrees()) / 2.0);
        if (Math.abs(getDegrees()-other.getDegrees()) > 180.) {
            result = result.reverse();
        }
        return result;
    }

    @Override
    public String toString() {
        return ""+getDegrees()+"°";
    }
    
    @Override
    public int hashCode() {
        return (int) (1023 ^ Double.doubleToLongBits(getDegrees()));
    }
    
    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof Bearing && getDegrees() == ((Bearing) object).getDegrees();
    }

    
    @Override
    public double getConfidence() {
        return DEFAULT_BEARING_CONFIDENCE;
    }

    @Override
    public ScalableValue<Pair<Double, Double>, BearingWithConfidence> getScalableValue() {
        return new ScalableBearing(this);
    }

    private static class ScalableBearing implements ScalableValue<Pair<Double, Double>, BearingWithConfidence> {
        private final double sin;
        private final double cos;
        
        public ScalableBearing(Bearing bearing) {
            this.sin = Math.sin(bearing.getRadians());
            this.cos = Math.cos(bearing.getRadians());
        }
        
        private ScalableBearing(double sin, double cos) {
            this.sin = sin;
            this.cos = cos;
        }
        
        @Override
        public ScalableValue<Pair<Double, Double>, BearingWithConfidence> multiply(double factor) {
            Pair<Double, Double> pair = getValue();
            return new ScalableBearing(factor*pair.getA(), factor*pair.getB());
        }

        @Override
        public ScalableValue<Pair<Double, Double>, BearingWithConfidence> add(ScalableValue<Pair<Double, Double>, BearingWithConfidence> t) {
            Pair<Double, Double> value = getValue();
            Pair<Double, Double> tValue = t.getValue();
            return new ScalableBearing(value.getA()+tValue.getA(), value.getB()+tValue.getB());
        }

        @Override
        public BearingWithConfidence divide(double divisor) {
            double angle;
            if (cos == 0) {
                angle = sin >= 0 ? Math.PI / 2 : -Math.PI / 2;
            } else {
                angle = Math.atan2(sin, cos);
            }
            BearingWithConfidence result = new RadianBearingImpl(angle < 0 ? angle + 2 * Math.PI : angle);
            return result;
        }

        @Override
        public Pair<Double, Double> getValue() {
            return new Pair<Double, Double>(sin, cos);
        }
    }
}
