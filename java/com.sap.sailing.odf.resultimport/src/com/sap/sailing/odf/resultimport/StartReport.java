package com.sap.sailing.odf.resultimport;

import java.text.ParseException;

import com.sap.sailing.domain.common.TimePoint;

public interface StartReport {
    String getDateAsString();
    
    TimePoint getTimePoint() throws ParseException;
    
    String getCourseAreaName();
    
    String getStartBearingAsString();
    
    String getWindSpeedAsString();
    
    String getWindDirectionAsString();
    
    Iterable<Start> getStarts();
    
    String getSourceName();
}
