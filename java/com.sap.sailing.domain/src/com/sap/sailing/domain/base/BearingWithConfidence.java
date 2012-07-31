package com.sap.sailing.domain.base;

import com.sap.sailing.domain.base.BearingWithConfidence.DoublePair;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.confidence.HasConfidenceAndIsScalable;


public interface BearingWithConfidence<RelativeTo> extends HasConfidenceAndIsScalable<DoublePair, Bearing, RelativeTo> {
    public static class DoublePair {
        private final double a;
        private final double b;
        
        public DoublePair(double a, double b) {
            super();
            this.a = a;
            this.b = b;
        }

        public double getA() {
            return a;
        }
        
        public double getB() {
            return b;
        }
    }
}
