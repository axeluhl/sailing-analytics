package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.base.PositionWithConfidence;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.PositionWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.RadianBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.confidence.ConfidenceBasedAveragerFactory;
import com.sap.sailing.domain.confidence.HasConfidence;
import com.sap.sailing.domain.confidence.HasConfidenceAndIsScalable;
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
        public Bearing divide(double divisor, double confidence) {
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
    
    private static class ScalableBearingWithConfidence<RelativeTo> extends ScalableBearing
    implements HasConfidenceAndIsScalable<Pair<Double, Double>, Bearing, RelativeTo> {
        private final double confidence;
        private final RelativeTo relativeTo;
        private final Bearing bearing;
        
        public ScalableBearingWithConfidence(Bearing bearing, double confidence, RelativeTo relativeTo) {
            super(bearing);
            this.confidence = confidence;
            this.bearing = bearing;
            this.relativeTo = relativeTo;
        }

        @Override
        public Bearing getObject() {
            return bearing;
        }

        @Override
        public double getConfidence() {
            return confidence;
        }

        @Override
        public ScalableValue<Pair<Double, Double>, Bearing> getScalableValue() {
            return this;
        }
        
        @Override
        public RelativeTo getRelativeTo() {
            return relativeTo;
        }
        
    }
    
    @Test
    public void testAveragingWithEmptyListYieldsNull() {
        ConfidenceBasedAverager<Double, Double, TimePoint> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        Set<HasConfidenceAndIsScalable<Double, Double, TimePoint>> emptySet = Collections.emptySet();
        HasConfidence<Double, Double, TimePoint> average = averager.getAverage(emptySet, MillisecondsTimePoint.now());
        assertNull(average);
    }

    @Test
    public void testAveragingWithNullArrayYieldsNull() {
        ConfidenceBasedAverager<Double, Double, TimePoint> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        HasConfidence<Double, Double, TimePoint> average = averager.getAverage(null, MillisecondsTimePoint.now());
        assertNull(average);
    }

    @Test
    public void testAveragingWithTwoDoubles() {
        ScalableDoubleWithConfidence<TimePoint> d1 = new ScalableDoubleWithConfidence<TimePoint>(1., 0.5, MillisecondsTimePoint.now());
        ScalableDoubleWithConfidence<TimePoint> d2 = new ScalableDoubleWithConfidence<TimePoint>(2., 0.5, MillisecondsTimePoint.now());
        ConfidenceBasedAverager<Double, Double, TimePoint> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        List<ScalableDoubleWithConfidence<TimePoint>> list = Arrays.asList(d1, d2);
        HasConfidence<Double, Double, TimePoint> average = averager.getAverage(list, MillisecondsTimePoint.now());
        assertEquals(1.5, average.getObject(), 0.00000001);
    }

    @Test
    public void testAveragingWithThreeDoubles() {
        ScalableDoubleWithConfidence<TimePoint> d1 = new ScalableDoubleWithConfidence<TimePoint>(1., 1., MillisecondsTimePoint.now());
        ScalableDoubleWithConfidence<TimePoint> d2 = new ScalableDoubleWithConfidence<TimePoint>(2., 1., MillisecondsTimePoint.now());
        ScalableDoubleWithConfidence<TimePoint> d3 = new ScalableDoubleWithConfidence<TimePoint>(3., 2., MillisecondsTimePoint.now());
        ConfidenceBasedAverager<Double, Double, TimePoint> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        List<ScalableDoubleWithConfidence<TimePoint>> list = Arrays.asList(d1, d2, d3);
        HasConfidence<Double, Double, TimePoint> average = averager.getAverage(list, MillisecondsTimePoint.now());
        assertEquals(2.25, average.getObject(), 0.00000001);
    }
    
    @Test
    public void testAveragingWithTwoBearings() {
        ScalableBearingWithConfidence<TimePoint> d1 = new ScalableBearingWithConfidence<TimePoint>(new DegreeBearingImpl(350.), 1., MillisecondsTimePoint.now());
        ScalableBearingWithConfidence<TimePoint> d2 = new ScalableBearingWithConfidence<TimePoint>(new DegreeBearingImpl(10.), 1., MillisecondsTimePoint.now());
        ConfidenceBasedAverager<Pair<Double, Double>, Bearing, TimePoint> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        List<ScalableBearingWithConfidence<TimePoint>> list = Arrays.asList(d1, d2);
        HasConfidence<Pair<Double, Double>, Bearing, TimePoint> average = averager.getAverage(list, MillisecondsTimePoint.now());
        assertEquals(0, average.getObject().getDegrees(), 0.00000001);
    }
    
    @Test
    public void testAveragingWithThreeBearings() {
        ScalableBearingWithConfidence<TimePoint> d1 = new ScalableBearingWithConfidence<TimePoint>(new DegreeBearingImpl(350.), 1., MillisecondsTimePoint.now());
        ScalableBearingWithConfidence<TimePoint> d2 = new ScalableBearingWithConfidence<TimePoint>(new DegreeBearingImpl(10.), 1., MillisecondsTimePoint.now());
        ScalableBearingWithConfidence<TimePoint> d3 = new ScalableBearingWithConfidence<TimePoint>(new DegreeBearingImpl(20.), 2., MillisecondsTimePoint.now());
        ConfidenceBasedAverager<Pair<Double, Double>, Bearing, TimePoint> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        List<ScalableBearingWithConfidence<TimePoint>> list = Arrays.asList(d1, d2, d3);
        HasConfidence<Pair<Double, Double>, Bearing, TimePoint> average = averager.getAverage(list, MillisecondsTimePoint.now());
        assertEquals(10, average.getObject().getDegrees(), 0.1);
    }

    @Test
    public void testAveragingTwoPositions() {
        PositionWithConfidence<TimePoint> p1 = new PositionWithConfidenceImpl<TimePoint>(new DegreePosition(0, 45), 0.9, MillisecondsTimePoint.now());
        PositionWithConfidence<TimePoint> p2 = new PositionWithConfidenceImpl<TimePoint>(new DegreePosition(0, -45), 0.9, MillisecondsTimePoint.now());
        ConfidenceBasedAverager<Triple<Double, Double, Double>, Position, TimePoint> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        List<PositionWithConfidence<TimePoint>> list = Arrays.asList(p1, p2);
        HasConfidence<Triple<Double, Double, Double>, Position, TimePoint> average = averager.getAverage(list, MillisecondsTimePoint.now());
        assertEquals(0, average.getObject().getLatDeg(), 0.1);
        assertEquals(0, average.getObject().getLngDeg(), 0.1);
    }

    @Test
    public void testAveragingTwoPositionsToNorthPole() {
        PositionWithConfidence<TimePoint> p1 = new PositionWithConfidenceImpl<TimePoint>(new DegreePosition(45, 90), 0.9, MillisecondsTimePoint.now());
        PositionWithConfidence<TimePoint> p2 = new PositionWithConfidenceImpl<TimePoint>(new DegreePosition(45, -90), 0.9, MillisecondsTimePoint.now());
        ConfidenceBasedAverager<Triple<Double, Double, Double>, Position, TimePoint> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        List<PositionWithConfidence<TimePoint>> list = Arrays.asList(p1, p2);
        HasConfidence<Triple<Double, Double, Double>, Position, TimePoint> average = averager.getAverage(list, MillisecondsTimePoint.now());
        assertEquals(90, average.getObject().getLatDeg(), 0.1);
        assertEquals(0, average.getObject().getLngDeg(), 0.1);
    }

    @Test
    public void testAveragingThreePositions() {
        PositionWithConfidence<TimePoint> p1 = new PositionWithConfidenceImpl<TimePoint>(new DegreePosition(49, 8), 0.9, MillisecondsTimePoint.now());
        PositionWithConfidence<TimePoint> p2 = new PositionWithConfidenceImpl<TimePoint>(new DegreePosition(49, 9), 0.9, MillisecondsTimePoint.now());
        PositionWithConfidence<TimePoint> p3 = new PositionWithConfidenceImpl<TimePoint>(new DegreePosition(50, 8.5), 0.9, MillisecondsTimePoint.now());
        ConfidenceBasedAverager<Triple<Double, Double, Double>, Position, TimePoint> averager = ConfidenceBasedAveragerFactory.INSTANCE.createAverager();
        List<PositionWithConfidence<TimePoint>> list = Arrays.asList(p1, p2, p3);
        HasConfidence<Triple<Double, Double, Double>, Position, TimePoint> average = averager.getAverage(list, MillisecondsTimePoint.now());
        assertTrue(average.getObject().getLatDeg() > 49);
        assertTrue(average.getObject().getLatDeg() < 50);
        assertEquals(8.5, average.getObject().getLngDeg(), 0.000000001);
    }
}
