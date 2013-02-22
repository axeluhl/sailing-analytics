package com.sap.sailing.odf.resultimport;

import java.text.ParseException;

import com.sap.sailing.domain.common.TimePoint;

public interface Start {
    String getBoatClass();
    
    Integer getRaceNumber();
    
    String getFleetName();
    
    String getCourseName();
    
    String getStartTimeAsString();
    
    TimePoint getTimePoint() throws ParseException;
    
    String getStartFlag();
    
    String getComment();
    
    Boolean isDoubleScore();
    
    Boolean isDiscardable();
}
