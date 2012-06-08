package com.sap.sailing.kiworesultimport;

import com.sap.sailing.domain.common.TimePoint;

public interface ResultList {
    String getLegende();
    
    String getImagePfad();
    
    String getStatus();
    
    String getBoatClass();
    
    String getEvent();
    
    String getTime();
    
    String getDate();
    
    TimePoint getTimePoint();
    
    Verteilung getVerteilung();
}
