package com.sap.sailing.domain.orc;

import java.util.Map;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;

//TODO COMMENTS!!!
public interface ORCCertificate {

    public ORCPerformanceCurve getPerformanceCurve (ORCPerformanceCurveCourse course);
    
    public double getGPH();
    
    public Map<Speed, Duration> getWindwardLeewardAllowances();
    
    public Map<Speed, Duration> getCircularRandomAllowances();
    
    public Map<Speed, Duration> getLongDistanceAllowances();
    
    public Map<Speed, Duration> getNonSpinnakerAllowances();
    
}