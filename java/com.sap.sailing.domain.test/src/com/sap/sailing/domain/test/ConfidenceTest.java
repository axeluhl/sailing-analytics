package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.domain.confidence.HasConfidence;
import com.sap.sailing.domain.confidence.ScalableValue;
import com.sap.sailing.domain.confidence.impl.ConfidenceBasedAveragerImpl;

public class ConfidenceTest {
    private static class ScalableDouble implements ScalableValue<Double, Double> {
        private final Double d;
        
        public ScalableDouble(Double d) {
            this.d = d;
        }

        @Override
        public ScalableValue<Double, Double> multiply(double factor) {
            return new ScalableDouble(d * factor);
        }

        @Override
        public ScalableValue<Double, Double> add(ScalableValue<Double, Double> t) {
            return new ScalableDouble(d+t.getValue());
        }

        @Override
        public Double divide(double divisor) {
            return d / divisor;
        }

        @Override
        public Double getValue() {
            return d;
        }
    }
    
    private static class ScalableDoubleWithConfidence extends ScalableDouble implements HasConfidence<Double, Double> {
        private final double confidence;
        
        public ScalableDoubleWithConfidence(double d, double confidence) {
            super(d);
            this.confidence = confidence;
        }
        
        @Override
        public double getConfidence() {
            return confidence;
        }

        @Override
        public ScalableValue<Double, Double> getScalableValue() {
            return this;
        }
    }
    
    @Test
    public void testAveragingSimpleDoubles() {
        ScalableDoubleWithConfidence d1 = new ScalableDoubleWithConfidence(1., 0.5);
        ScalableDoubleWithConfidence d2 = new ScalableDoubleWithConfidence(2., 0.5);
        @SuppressWarnings("unchecked")
        Double average = new ConfidenceBasedAveragerImpl<Double, Double>().getAverage(d1, d2);
        assertEquals(1.5, average, 0.00000001);
    }
}
