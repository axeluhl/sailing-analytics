package com.sap.sailing.kiworesultimport;

import java.text.ParseException;

import com.sap.sailing.domain.common.TimePoint;

public interface Startbericht {
    String getDatum();
    
    TimePoint getTimePoint() throws ParseException;
    
    String getRegattabahn();
    
    String getKompasskurs();
    
    String getWindstaerke();
    
    String getWindrichtung();
    
    Iterable<Start> getStarts();
}
