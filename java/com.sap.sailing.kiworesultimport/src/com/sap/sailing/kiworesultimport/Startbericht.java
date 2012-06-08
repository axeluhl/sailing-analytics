package com.sap.sailing.kiworesultimport;

import com.sap.sailing.domain.common.TimePoint;

public interface Startbericht {
    String getDatum();
    
    TimePoint getTimePoint();
    
    String getRegattabahn();
    
    String getKompasskurs();
    
    String getWindstaerke();
    
    String getWindrichtung();
    
    Iterable<Start> getStarts();
}
