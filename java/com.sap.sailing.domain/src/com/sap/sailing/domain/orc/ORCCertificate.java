package com.sap.sailing.domain.orc;

import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.ranking.RankingMetric;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;

//TODO COMMENTS!!!
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
public interface ORCCertificate {

    /**
     * Returns a {@link ORCPerformanceCurve} for the competitor owning this {@link ORCCertificate}.
     * 
     * @param course
     *            equals the {@link ORCPerformanceCurveCourse} (part of the whole course) sailed by the competitor upon
     *            the point of this call.
     * @return performance curve to calculate the implied wind for the boat and the sailed part of the course so the
     *         performance can be compared with other competitors.
     */
    public ORCPerformanceCurve getPerformanceCurve(ORCPerformanceCurveCourse course);
    
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
     * 
     * @return
     */
    public Map<Speed, Duration> getWindwardLeewardAllowances();
    
    /**
     * 
     * @return 
     */
    public Map<Speed, Duration> getCircularRandomAllowances();
    
    /**
     * 
     * @return
     */
    public Map<Speed, Duration> getLongDistanceAllowances();
    
    /**
     * 
     * @return
     */
    public Map<Speed, Duration> getNonSpinnakerAllowances();
    
}