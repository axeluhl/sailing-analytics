package com.sap.sailing.kiworesultimport;

/**
 * Derived object, providing a view onto the data parsed from the result ZIP
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface RaceSummary {
    String getBoatClassName();
    
    int getRaceNumber();
    
    Iterable<String> getFleetNames();
    
    /**
     * The {@link Boat} objects 
     */
    Iterable<Boat> getBoats();
    
    /**
     * Retrieves the {@link BoatResultInRace} data that <code>boat</code> achieved in the race summarized by this object
     */
    BoatResultInRace getBoatResults(Boat boat);

    Boat getBoat(String sailingNumber);
}
