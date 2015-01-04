package com.sap.sailing.polars.test;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

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
    
}
