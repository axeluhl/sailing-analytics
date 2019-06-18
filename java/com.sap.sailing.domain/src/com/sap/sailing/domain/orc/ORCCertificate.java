package com.sap.sailing.domain.orc;

import java.util.Map;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;

//TODO COMMENTS!!!
/**
 * Represents semantically a real ORC certificate for a {@link competitor}. 
 * @author Daniel Lisunkin (i505543)
 *
 */
public interface ORCCertificate {

    /**
     * Returns a {@link ORCPerformanceCurve} for the competitor owning this {@link ORCCertificate}.
     * 
     * @param course
     *          equals the {@link ORCPerformanceCurveCourse} (part of the whole course) sailed by the competitor upon the point of this call.
     * @return
     */
    public ORCPerformanceCurve getPerformanceCurve(ORCPerformanceCurveCourse course);
    
    /**
     * 
     * @return
     */
    public double getGPH();
    
    /**
     * 
     * @return
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
     * @returny
     */
    public Map<Speed, Duration> getLongDistanceAllowances();
    
    /**
     * 
     * @return
     */
    public Map<Speed, Duration> getNonSpinnakerAllowances();
    
}