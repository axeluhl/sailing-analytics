package com.sap.sailing.kiworesultimport;

import java.text.ParseException;

import com.sap.sse.common.TimePoint;

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
