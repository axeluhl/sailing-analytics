package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.RadianBearingImpl;
import com.sap.sailing.domain.common.Util.Pair;
import com.sap.sailing.domain.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.confidence.ConfidenceBasedAveragerFactory;
import com.sap.sailing.domain.confidence.HasConfidence;
import com.sap.sailing.domain.confidence.ScalableValue;
import com.sap.sailing.domain.confidence.impl.ScalableDoubleWithConfidence;

public class ConfidenceTest {
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

        @Override
        public Bearing divide(double divisor) {
            double angle;
            if (cos == 0) {
                angle = sin >= 0 ? Math.PI / 2 : -Math.PI / 2;
            } else {
                angle = Math.atan2(sin, cos);
            }
            Bearing result = new RadianBearingImpl(angle < 0 ? angle + 2 * Math.PI : angle);
            return result;
        }

        @Override
        public Pair<Double, Double> getValue() {
            return new Pair<Double, Double>(sin, cos);
        }
    }
    
    private static class ScalableBearingWithConfidence extends ScalableBearing implements HasConfidence<Pair<Double, Double>, Bearing> {
        private final double confidence;
        
        public ScalableBearingWithConfidence(Bearing bearing, double confidence) {
            super(bearing);
            this.confidence = confidence;
        }

        @Override
        public double getConfidence() {
            return confidence;
        }

        @Override
        public ScalableValue<Pair<Double, Double>, Bearing> getScalableValue() {
            return this;
        }
        
    }
    
    @Test
    public void testAveragingWithEmptyListYieldsNull() {
        ConfidenceBasedAverager<Double, Double> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        Set<HasConfidence<Double, Double>> emptySet = Collections.emptySet();
        Double average = averager.getAverage(emptySet);
        assertNull(average);
    }

    @Test
    public void testAveragingWithNullArrayYieldsNull() {
        ConfidenceBasedAverager<Double, Double> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        Double average = averager.getAverage(null);
        assertNull(average);
    }

    @Test
    public void testAveragingWithTwoDoubles() {
        ScalableDoubleWithConfidence d1 = new ScalableDoubleWithConfidence(1., 0.5);
        ScalableDoubleWithConfidence d2 = new ScalableDoubleWithConfidence(2., 0.5);
        ConfidenceBasedAverager<Double, Double> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        List<ScalableDoubleWithConfidence> list = Arrays.asList(d1, d2);
        Double average = averager.getAverage(list);
        assertEquals(1.5, average, 0.00000001);
    }

    @Test
    public void testAveragingWithThreeDoubles() {
        ScalableDoubleWithConfidence d1 = new ScalableDoubleWithConfidence(1., 1.);
        ScalableDoubleWithConfidence d2 = new ScalableDoubleWithConfidence(2., 1.);
        ScalableDoubleWithConfidence d3 = new ScalableDoubleWithConfidence(3., 2.);
        ConfidenceBasedAverager<Double, Double> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        List<ScalableDoubleWithConfidence> list = Arrays.asList(d1, d2, d3);
        Double average = averager.getAverage(list);
        assertEquals(2.25, average, 0.00000001);
    }
    
    @Test
    public void testAveragingWithTwoBearings() {
        ScalableBearingWithConfidence d1 = new ScalableBearingWithConfidence(new DegreeBearingImpl(350.), 1.);
        ScalableBearingWithConfidence d2 = new ScalableBearingWithConfidence(new DegreeBearingImpl(10.), 1.);
        ConfidenceBasedAverager<Pair<Double, Double>, Bearing> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        List<ScalableBearingWithConfidence> list = Arrays.asList(d1, d2);
        Bearing average = averager.getAverage(list);
        assertEquals(0, average.getDegrees(), 0.00000001);
    }
    
    @Test
    public void testAveragingWithThreeoBearings() {
        ScalableBearingWithConfidence d1 = new ScalableBearingWithConfidence(new DegreeBearingImpl(350.), 1.);
        ScalableBearingWithConfidence d2 = new ScalableBearingWithConfidence(new DegreeBearingImpl(10.), 1.);
        ScalableBearingWithConfidence d3 = new ScalableBearingWithConfidence(new DegreeBearingImpl(20.), 2.);
        ConfidenceBasedAverager<Pair<Double, Double>, Bearing> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        List<ScalableBearingWithConfidence> list = Arrays.asList(d1, d2, d3);
        Bearing average = averager.getAverage(list);
        assertEquals(10, average.getDegrees(), 0.1);
    }
    
}
