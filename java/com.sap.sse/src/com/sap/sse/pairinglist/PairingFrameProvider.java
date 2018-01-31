package com.sap.sse.pairinglist;

/**
 * 
 * This interface is used to get the required parameters for the calculation of a {@link PairingListTemplate}. 
 * 
 * @author D070307
 *
 */
public interface PairingFrameProvider {
    /**
     * Returns the count of all flights.
     * 
     * @return flight count
     */
    int getFlightsCount();
    
    /**
     * Returns the count of groups within a single flight.
     * 
     * @return group count
     */
    int getGroupsCount();

    /**
     * Returns the count of competitors.
     * 
     * @return competitor count.
     */
    int getCompetitorsCount();
}
