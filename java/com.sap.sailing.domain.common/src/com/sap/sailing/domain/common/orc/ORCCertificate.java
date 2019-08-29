package com.sap.sailing.domain.common.orc;

import java.io.Serializable;
import java.util.Map;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;

/**
 * Represents semantically a real ORC certificate for a {@link Competitor}, which is used to rate different type of
 * boats for different insohre and offshore race conditions.
 * <p>
 * An ORC certificate is issued by the "Member National Authorities" of World Sailing and are available for insight at
 * https://www.orc.org/index.asp . Other information about the whole scoring system and different variants are available
 * too.
 * <p>
 * One implementing class provides all necessary functionalities to score the Competitors with a choosen
 * {@link RankingMetric}.
 * 
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public interface ORCCertificate extends Serializable {

    /*
     * Returns a {@link ORCPerformanceCurve} for the competitor owning this {@link ORCCertificate}.
     * 
     * @param course
     *            equals the {@link ORCPerformanceCurveCourse} (part of the whole course) sailed by the competitor upon
     *            the point of this call.
     * @return performance curve to calculate the implied wind for the boat and the sailed part of the course so the
     *         performance can be compared with other competitors.
     */
    //public ORCPerformanceCurve getPerformanceCurve(ORCPerformanceCurveCourse course) throws FunctionEvaluationException;
    
    /**
     * Returns the sailnumber of the {@link Competitor} which this certificate belongs to.
     * 
     * @return sailnumber as a string, which consists out of some alphanumeric characters (most time the nation or
     *         boatclass id), a blank space and some numerical digits
     */
    public String getSailnumber();
    
    /**
     * Returns the boatclass of the {@link Competitor} which this certificate belongs to.
     */
    public String getBoatclass();
    
    /**
     * Returns the GPH value for the {@link Competitor}. The GPH value represents the overall performance of the boat.
     * The value itself is again an allowance (in seconds per nautical mile) and could be used as a ToD Factor.
     * Most of the times it is used to divide a big fleet into similar fast divisions.
     */
    public double getGPH();
    
    /**
     * Returns the CDL (Class Division Length) value for the {@link Competitor}. This value is another (and newer) approach to rate the overall performance of different boats.
     * The different division intervals are - in contrast to the intervals of the GPH - set by the Offshore Race Committee and not by the national association for a uniform handling.
     * The higher the value, the higher the overall performance, it is measured in meters.
     */
    public double getCDL();
    
    /**
     * Returns the LOA (length over all) of the {@link Competitor} boat which this certificate belongs to.
     */
    public Distance getLengthOverAll();
    
    /**
     * Returns a Map of speed predictions (in knots) for different wind speeds to use for a {@link ORCPerformanceCurve} rating
     * in a race, where the upwind and the downwind part both conrtibute 50% to the total course.
     * 
     * @return Map with elements of type wind {@link Speed} as keys and {@link Duration}s equaling a time allowance per
     *         nautical mile as values.
     */
    public Map<Speed, Speed> getWindwardLeewardSpeedPrediction();
    
    /**
     * Returns a Map of speed predictions (in knots) for different wind speeds to use for a {@link ORCPerformanceCurve} rating
     * in a race, where the course resembles a circular figure.
     * 
     * @return Map with elements of type wind {@link Speed} as keys and {@link Duration}s equaling a time allowance per
     *         nautical mile as values.
     */
    public Map<Speed, Speed> getCircularRandomSpeedPredictions();
    
    /**
     * Returns a Map of speed predictions (in knots) for different wind speeds to use for a {@link ORCPerformanceCurve} rating
     * in a race, where the race is a long distance offshore/coastal race.
     * 
     * @return Map with elements of type wind {@link Speed} as keys and {@link Duration}s equaling a time allowance per
     *         nautical mile as values.
     */
    public Map<Speed, Speed> getLongDistanceSpeedPredictions();
    
    /**
     * Returns a Map of speed predictions (in knots)  for different wind speeds to use for a {@link ORCPerformanceCurve} rating
     * in a race, where the competitor doesn't use any spinnaker.
     * 
     * @return Map with elements of type wind {@link Speed} as keys and {@link Duration}s equaling a time allowance per
     *         nautical mile as values.
     */
    public Map<Speed, Speed> getNonSpinnakerSpeedPredictions();
    
    public Map<Speed, Bearing> getBeatAngles();
    
    public Map<Speed, Bearing> getRunAngles();
    
    public Map<Speed, Duration> getBeatAllowances();
    
    public Map<Speed, Duration> getRunAllowances();
    
    public Map<Speed, Speed> getBeatVMGPredictions();
    
    public Map<Speed, Speed> getRunVMGPredictions();
    
    public Map<Speed, Map<Bearing, Speed>> getVelocityPredictionPerTrueWindSpeedAndAngle();
    
}