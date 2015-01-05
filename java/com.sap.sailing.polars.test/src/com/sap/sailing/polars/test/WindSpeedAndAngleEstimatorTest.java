package com.sap.sailing.polars.test;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.polars.regression.impl.WindSpeedAndAngleEstimator;
import com.sap.sse.common.Util.Pair;

public class WindSpeedAndAngleEstimatorTest {
    
    private static final double ERROR = 0.0001;

    @Test
    public void testAverageTrueWindSpeedAndAngleEstimationForSteadyGrowth() {
        List<Pair<Speed, SpeedWithBearingWithConfidence<Void>>> averageBoatSpeedAndCourseForWindSpeed = new ArrayList<>();
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(5), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(3,
                        new DegreeBearingImpl(46)), 0.4, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(6), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4,
                        new DegreeBearingImpl(47)), 0.6, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(7), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4.5,
                        new DegreeBearingImpl(47.5)), 0.5, null)));
        WindSpeedAndAngleEstimator estimator = new WindSpeedAndAngleEstimator(averageBoatSpeedAndCourseForWindSpeed);
        
        SpeedWithBearingWithConfidence<Void> result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(3));
        assertThat(result.getObject().getKnots(), closeTo(5, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(46, ERROR));
        assertThat(result.getConfidence(), closeTo(0.4, ERROR));
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(4));
        assertThat(result.getObject().getKnots(), closeTo(6, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(47, ERROR));
        assertThat(result.getConfidence(), closeTo(0.6, ERROR));
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(4.5));
        assertThat(result.getObject().getKnots(), closeTo(7, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(47.5, ERROR));
        assertThat(result.getConfidence(), closeTo(0.5, ERROR));
        
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(4.25));
        assertThat(result.getObject().getKnots(), closeTo(6.5, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(47.25, ERROR));
        assertThat(result.getConfidence(), closeTo(0.55, ERROR));
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(3.25));
        assertThat(result.getObject().getKnots(), closeTo(5.25, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(46.25, ERROR));
        assertThat(result.getConfidence(), closeTo(0.45, ERROR));
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(2.75));
        assertThat(result.getObject().getKnots(), closeTo(5, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(46, ERROR));
        assertThat(result.getConfidence(), closeTo(0.2, ERROR));
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(4.75));
        assertThat(result.getObject().getKnots(), closeTo(7, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(47.5, ERROR));
        assertThat(result.getConfidence(), closeTo(0.25, ERROR));
             
    }

    /**
     * Not a realistic scenario in the sailing world, but testing for the sake of correctness of the algorithm
     */
    @Test
    public void testAverageTrueWindSpeedAndAngleEstimationForSteadyDeclination() {
        List<Pair<Speed, SpeedWithBearingWithConfidence<Void>>> averageBoatSpeedAndCourseForWindSpeed = new ArrayList<>();
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(7), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(3,
                        new DegreeBearingImpl(46)), 0.4, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(6), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4,
                        new DegreeBearingImpl(47)), 0.6, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(5), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4.5,
                        new DegreeBearingImpl(47.5)), 0.5, null)));
        WindSpeedAndAngleEstimator estimator = new WindSpeedAndAngleEstimator(averageBoatSpeedAndCourseForWindSpeed);
        
        SpeedWithBearingWithConfidence<Void> result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(3));
        assertThat(result.getObject().getKnots(), closeTo(7, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(46, ERROR));
        assertThat(result.getConfidence(), closeTo(0.4, ERROR));
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(4));
        assertThat(result.getObject().getKnots(), closeTo(6, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(47, ERROR));
        assertThat(result.getConfidence(), closeTo(0.6, ERROR));
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(4.5));
        assertThat(result.getObject().getKnots(), closeTo(5, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(47.5, ERROR));
        assertThat(result.getConfidence(), closeTo(0.5, ERROR));
        
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(4.25));
        assertThat(result.getObject().getKnots(), closeTo(5.5, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(47.25, ERROR));
        assertThat(result.getConfidence(), closeTo(0.55, ERROR));
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(3.25));
        assertThat(result.getObject().getKnots(), closeTo(6.75, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(46.25, ERROR));
        assertThat(result.getConfidence(), closeTo(0.45, ERROR));
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(2.75));
        assertThat(result.getObject().getKnots(), closeTo(7, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(46, ERROR));
        assertThat(result.getConfidence(), closeTo(0.2, ERROR));
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(4.75));
        assertThat(result.getObject().getKnots(), closeTo(5, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(47.5, ERROR));
        assertThat(result.getConfidence(), closeTo(0.25, ERROR));
             
    }
    
    @Test
    public void testAverageTrueWindSpeedAndAngleEstimationNegative() {
        List<Pair<Speed, SpeedWithBearingWithConfidence<Void>>> averageBoatSpeedAndCourseForWindSpeed = new ArrayList<>();
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(5), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(3,
                        new DegreeBearingImpl(46)), 0.4, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(6), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4,
                        new DegreeBearingImpl(47)), 0.6, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(7), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4.5,
                        new DegreeBearingImpl(47.5)), 0.5, null)));
        WindSpeedAndAngleEstimator estimator = new WindSpeedAndAngleEstimator(averageBoatSpeedAndCourseForWindSpeed);
        
        SpeedWithBearingWithConfidence<Void> result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(2));
        assertThat(result, nullValue());
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(5.5));
        assertThat(result, nullValue());
             
    }
    
    @Test
    public void testAverageTrueWindSpeedAndAngleEstimationForNonReversibleFunction() {
        List<Pair<Speed, SpeedWithBearingWithConfidence<Void>>> averageBoatSpeedAndCourseForWindSpeed = new ArrayList<>();
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(5), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(3,
                        new DegreeBearingImpl(46)), 0.4, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(6), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4,
                        new DegreeBearingImpl(47)), 0.6, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(7), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4.5,
                        new DegreeBearingImpl(47.5)), 0.5, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(8), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4.25,
                        new DegreeBearingImpl(47.5)), 0.25, null)));
        WindSpeedAndAngleEstimator estimator = new WindSpeedAndAngleEstimator(averageBoatSpeedAndCourseForWindSpeed);
        
        SpeedWithBearingWithConfidence<Void> result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(4.25));
        assertThat(result.getObject().getKnots(), closeTo(6.5, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(47.25, ERROR));
        assertThat(result.getConfidence(), closeTo(0.55, ERROR));
                
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testAverageTrueWindSpeedAndAngleEstimationWithAllCandidatesReturned() {
        List<Pair<Speed, SpeedWithBearingWithConfidence<Void>>> averageBoatSpeedAndCourseForWindSpeed = new ArrayList<>();
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(5), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(3,
                        new DegreeBearingImpl(46)), 0.4, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(6), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4,
                        new DegreeBearingImpl(47)), 0.6, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(7), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4.5,
                        new DegreeBearingImpl(47.5)), 0.5, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(8), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4.25,
                        new DegreeBearingImpl(47.5)), 0.25, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(9), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(3,
                        new DegreeBearingImpl(47.5)), 0.25, null)));
        WindSpeedAndAngleEstimator estimator = new WindSpeedAndAngleEstimator(averageBoatSpeedAndCourseForWindSpeed);

        Set<SpeedWithBearingWithConfidence<Void>> result = estimator
                .getAverageTrueWindSpeedAndAngleCandidates(new KnotSpeedImpl(4.3));
        assertThat(result.size(), is(2));
        for (SpeedWithBearingWithConfidence<Void> candidate : result) {
            assertTrue(Math.abs(candidate.getConfidence()-0.2999999)<=ERROR ||
                    Math.abs(candidate.getConfidence()-0.54)<=ERROR);
            if (Math.abs(candidate.getConfidence() - 0.2999999) < ERROR) {
                assertThat(candidate.getObject().getKnots(), closeTo(7.8, ERROR));
                assertThat(candidate.getObject().getBearing().getDegrees(), closeTo(47.5, ERROR));
            } else {
                assertThat(candidate.getObject().getKnots(), closeTo(6.6, ERROR));
                assertThat(candidate.getObject().getBearing().getDegrees(), closeTo(47.3, ERROR));
                
            }
        }
        
        result = estimator
                .getAverageTrueWindSpeedAndAngleCandidates(new KnotSpeedImpl(2.9));
        assertThat(result.size(), is(2));
        for (SpeedWithBearingWithConfidence<Void> candidate : result) {
            assertThat(candidate.getConfidence(), anyOf(closeTo(0.2, ERROR), closeTo(0.125, ERROR)));
            if (Math.abs(candidate.getConfidence() - 0.2) < ERROR) {
                assertThat(candidate.getObject().getKnots(), closeTo(5, ERROR));
                assertThat(candidate.getObject().getBearing().getDegrees(), closeTo(46, ERROR));
            } else {
                assertThat(candidate.getObject().getKnots(), closeTo(9, ERROR));
                assertThat(candidate.getObject().getBearing().getDegrees(), closeTo(47.5, ERROR));
                
            }
        }
        
        result = estimator
                .getAverageTrueWindSpeedAndAngleCandidates(new KnotSpeedImpl(4.9));
        assertThat(result.size(), is(1));
        SpeedWithBearingWithConfidence<Void> candidate = result.iterator().next();
        assertThat(candidate.getObject().getKnots(), closeTo(7, ERROR));
        assertThat(candidate.getObject().getBearing().getDegrees(), closeTo(47.5, ERROR));
        assertThat(candidate.getConfidence(), closeTo(0.25, ERROR));
        
        
                
    }
    
    @Test
    public void testAverageTrueWindSpeedAndAngleEstimationForTwoSamplingPointsWithSameBoatSpeed() {
        List<Pair<Speed, SpeedWithBearingWithConfidence<Void>>> averageBoatSpeedAndCourseForWindSpeed = new ArrayList<>();
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(7), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4.5,
                        new DegreeBearingImpl(47.5)), 0.5, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(8), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(4.5,
                        new DegreeBearingImpl(47.5)), 0.4, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(9), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(5.5,
                        new DegreeBearingImpl(47.5)), 0.4, null)));
        averageBoatSpeedAndCourseForWindSpeed.add(new Pair<Speed, SpeedWithBearingWithConfidence<Void>>(
                new KnotSpeedImpl(10), new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(5.5,
                        new DegreeBearingImpl(47.5)), 0.5, null)));
        WindSpeedAndAngleEstimator estimator = new WindSpeedAndAngleEstimator(averageBoatSpeedAndCourseForWindSpeed);
        
        SpeedWithBearingWithConfidence<Void> result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(4.5));
        assertThat(result.getObject().getKnots(), closeTo(7.44444, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(47.5, ERROR));
        assertThat(result.getConfidence(), closeTo(0.455555, ERROR));
        
        result = estimator.getAverageTrueWindSpeedAndAngle(new KnotSpeedImpl(5.5));
        assertThat(result.getObject().getKnots(), closeTo(9.555555, ERROR));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(47.5, ERROR));
        assertThat(result.getConfidence(), closeTo(0.455555, ERROR));
        
        
             
    }
    
}
