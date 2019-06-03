package com.sap.sailing.domain.orc;

import java.util.Map;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;

//TODO COMMENTS!!!
/**
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public interface ORCCertificate {

    /**
     * 
     * @param course
     * @return
     */
    public ORCPerformanceCurve getPerformanceCurve (ORCPerformanceCurveCourse course);
    
    /**
     * 
     * @return
     */
    public double getGPH();
    
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