package com.sap.sailing.kiworesultimport;

import java.text.ParseException;

import com.sap.sailing.domain.common.TimePoint;

public interface Start {
    String getBoatClass();
    
    Integer getRaceNumber();
    
    String getFleetName();
    
    String getKurs();
    
    String getStartzeit();
    
    TimePoint getTimePoint() throws ParseException;
    
    String getStartflagge();
    
    String getBemerkung();
    
    Boolean getDoppelteWertung();
    
    Boolean getStreichbar();
}
