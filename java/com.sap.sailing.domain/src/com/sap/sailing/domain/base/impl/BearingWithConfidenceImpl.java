package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.BearingWithConfidence;
import com.sap.sailing.domain.common.Util.Pair;
import com.sap.sailing.domain.confidence.ScalableValue;

public class BearingWithConfidenceImpl implements BearingWithConfidence {
    private final Bearing bearing;
    
    private final double confidence;
    
    public BearingWithConfidenceImpl(Bearing bearing, double confidence) {
        this.bearing = bearing;
        this.confidence = confidence;
    }
    
    @Override
    public Bearing getBearing() {
        return bearing;
    }

    @Override
    public double getConfidence() {
        return confidence;
    }

    @Override
    public ScalableValue<Pair<Double, Double>, BearingWithConfidence> getScalableValue() {
        return new ScalableBearing(getBearing());
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
        public BearingWithConfidence divide(double divisor, double confidence) {
            double angle;
            if (cos == 0) {
                angle = sin >= 0 ? Math.PI / 2 : -Math.PI / 2;
            } else {
                angle = Math.atan2(sin, cos);
            }
            BearingWithConfidence result = new BearingWithConfidenceImpl(new RadianBearingImpl(angle < 0 ? angle + 2 * Math.PI : angle), confidence);
            return result;
        }

        @Override
        public Pair<Double, Double> getValue() {
            return new Pair<Double, Double>(sin, cos);
        }
    }
}
