package com.sap.sailing.kiworesultimport;

import com.sap.sse.common.TimePoint;

/**
 * Derived object, providing a view onto the data parsed from the result ZIP
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface RegattaSummary {
    TimePoint getTimePointPublished();
    
    String getBoatClassName();
    
    /**
     * Returns the races in ascending race number order
     */
    Iterable<RaceSummary> getRaces();
    
    RaceSummary getRace(int raceNumberOneBased);
    
    /**
     * All {@link Boat} objects that participated in this regatta
     */
    Iterable<Boat> getBoats();
    
    String getEventName();
}
