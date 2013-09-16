package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.confidence.ScalableValue;

public class SpeedWithConfidenceImpl<RelativeTo> extends HasConfidenceImpl<Double, Speed, RelativeTo> implements SpeedWithConfidence<RelativeTo> {
    private static final long serialVersionUID = -3122580369653833748L;

    public SpeedWithConfidenceImpl(Speed object, double confidence, RelativeTo relativeTo) {
        super(object, confidence, relativeTo);
    }

    @Override
    public ScalableValue<Double, Speed> getScalableValue() {
        return new ScalableSpeed(getObject());
    }
    
    private static class ScalableSpeed implements ScalableValue<Double, Speed> {
        private final double knots;
        
        public ScalableSpeed(Speed speed) {
            this.knots = speed.getKnots();
        }
        
        private ScalableSpeed(double knots) {
            this.knots = knots;
        }

        @Override
        public ScalableValue<Double, Speed> multiply(double factor) {
            return new ScalableSpeed(factor*knots);
        }

        @Override
        public ScalableValue<Double, Speed> add(ScalableValue<Double, Speed> t) {
            return new ScalableSpeed(knots+t.getValue());
        }

        @Override
        public Speed divide(double divisor) {
            return new KnotSpeedImpl(knots / divisor);
        }

        @Override
        public Double getValue() {
            return knots;
        }
    }
}
