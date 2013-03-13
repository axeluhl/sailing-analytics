package com.sap.sailing.odf.resultimport;

import com.sap.sailing.domain.common.TimePoint;

public interface OdfBody {
    Iterable<Competition> getCompetitions();
    
    String getDocumentSubtype();
    
    String getResultStatus();
    
    String getVersion();
    
    String getFeedFlag();
    
    TimePoint getTimePoint();
    
    String getVenue();

    String getEventName();

    String getBoatClassName();
}
