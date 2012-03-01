package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.BearingWithConfidence;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.confidence.IsScalable;
import com.sap.sailing.domain.confidence.ScalableValue;

public class BearingWithConfidenceImpl<RelativeTo> extends HasConfidenceImpl<Pair<Double, Double>, Bearing, RelativeTo>
implements BearingWithConfidence<RelativeTo>, IsScalable<Pair<Double, Double>, Bearing> {
    public BearingWithConfidenceImpl(Bearing bearing, double confidence, RelativeTo relativeTo) {
        super(bearing, confidence, relativeTo);
    }
    
    @Override
    public ScalableValue<Pair<Double, Double>, Bearing> getScalableValue() {
        return new ScalableBearing(getObject());
    }

    private static class ScalableBearing implements ScalableValue<Pair<Double, Double>, Bearing> {
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
        public ScalableValue<Pair<Double, Double>, Bearing> multiply(double factor) {
            Pair<Double, Double> pair = getValue();
            return new ScalableBearing(factor*pair.getA(), factor*pair.getB());
        }

        @Override
        public ScalableValue<Pair<Double, Double>, Bearing> add(ScalableValue<Pair<Double, Double>, Bearing> t) {
            Pair<Double, Double> value = getValue();
            Pair<Double, Double> tValue = t.getValue();
            return new ScalableBearing(value.getA()+tValue.getA(), value.getB()+tValue.getB());
        }

        /**
         * If the combined confidence was 0.0, no {@link Bearing} object can reasonably be computed; hence, <code>null</code>
         * is returned in such cases.
         */
        @Override
        public Bearing divide(double divisor) {
            Bearing result;
            if (sin == 0 && cos == 0) {
                result = null;
            } else {
                double angle;
                if (cos == 0) {
                    angle = sin >= 0 ? Math.PI / 2 : -Math.PI / 2;
                } else {
                    angle = Math.atan2(sin, cos);
                }
                result = new RadianBearingImpl(angle < 0 ? angle + 2 * Math.PI : angle);
            }
            return result;
        }

        @Override
        public Pair<Double, Double> getValue() {
            return new Pair<Double, Double>(sin, cos);
        }
    }
}
