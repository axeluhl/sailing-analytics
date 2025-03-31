package com.sap.sailing.kiworesultimport;

import com.sap.sse.common.TimePoint;

public interface ResultList {
    String getLegend();
    
    String getImagePath();
    
    String getStatus();
    
    String getBoatClassName();
    
    String getEvent();
    
    String getTime();
    
    String getDate();
    
    /**
     * Point in time when this result list was published
     */
    TimePoint getTimePointPublished();
    
    Iterable<Boat> getBoats();
    
    /**
     * Matches <code>sailID</code> with {@link Boat#getSailingNumber()}
     * 
     * @return <code>null</code> if no such {@link Boat} is found in {@link #getBoats}, or the boat found otherwise.
     */
    Boat getBoatBySailID(String sailID);

    String getSourceName();

    Iterable<Integer> getRaceNumbers();
}
