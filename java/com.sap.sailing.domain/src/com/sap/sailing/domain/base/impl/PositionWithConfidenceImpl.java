package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.PositionWithConfidence;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.RadianPosition;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.confidence.IsScalable;
import com.sap.sailing.domain.confidence.ScalableValue;

public class PositionWithConfidenceImpl<RelativeTo> extends HasConfidenceImpl<Triple<Double, Double, Double>, Position, RelativeTo> implements
        PositionWithConfidence<RelativeTo>, IsScalable<Triple<Double, Double, Double>, Position> {
    public PositionWithConfidenceImpl(Position position, double confidence, RelativeTo relativeTo) {
        super(position, confidence, relativeTo);
    }

    @Override
    public ScalableValue<Triple<Double, Double, Double>, Position> getScalableValue() {
        return new ScalablePosition(getObject());
    }

    private static class ScalablePosition implements ScalableValue<Triple<Double, Double, Double>, Position> {
        private final double x, y, z;
        
        public ScalablePosition(Position position) {
            this(Math.cos(position.getLatRad()) * Math.cos(position.getLngRad()),
                    Math.cos(position.getLatRad()) * Math.sin(position.getLngRad()),
                    Math.sin(position.getLatRad()));
        }
        
        public ScalablePosition(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        @Override
        public ScalableValue<Triple<Double, Double, Double>, Position> multiply(double factor) {
            return new ScalablePosition(factor*x, factor*y, factor*z);
        }

        @Override
        public ScalableValue<Triple<Double, Double, Double>, Position> add(
                ScalableValue<Triple<Double, Double, Double>, Position> t) {
            return new ScalablePosition(x+t.getValue().getA(), y+t.getValue().getB(), z+t.getValue().getC());
        }

        @Override
        public Position divide(double divisor, double confidence) {
            // don't need to scale down; atan2 is agnostic regarding scaling factors
            double hyp = Math.sqrt(x * x + y * y);
            double latRad = Math.atan2(z, hyp);
            double lngRad = Math.atan2(y, x);
            return new RadianPosition(latRad, lngRad);
        }

        @Override
        public Triple<Double, Double, Double> getValue() {
            return new Triple<Double, Double, Double>(x, y, z);
        }
        
    }
}
