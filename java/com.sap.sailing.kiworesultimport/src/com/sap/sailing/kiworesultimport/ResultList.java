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
    
    /**
     * Point in time when this result list was published
     */
    TimePoint getTimePoint();
    
    Iterable<Boat> getBoats();
    
    /**
     * Matches <code>sailID</code> with {@link Boat#getSailingNumber()}
     * 
     * @return <code>null</code> if no such {@link Boat} is found in {@link #getBoats}, or the boat found otherwise.
     */
    Boat getBoatBySailID(String sailID);
}
