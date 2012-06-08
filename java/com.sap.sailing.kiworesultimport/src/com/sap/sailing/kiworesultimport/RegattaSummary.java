package com.sap.sailing.kiworesultimport;

/**
 * Derived object, providing a view onto the data parsed from the result ZIP
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface RegattaSummary {
    String getBoatClassName();
    
    Iterable<RaceSummary> getRaces();
    
    RaceSummary getRace(int raceNumberOneBased);
    
    /**
     * All {@link Boat} objects that participated in this regatta
     */
    Iterable<Boat> getBoats();
}
