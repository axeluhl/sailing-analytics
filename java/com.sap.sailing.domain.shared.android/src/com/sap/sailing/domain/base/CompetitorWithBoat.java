package com.sap.sailing.domain.base;

/**
 * A competitor having an assigned boat
 * This makes sense e.g. in the context of a race where a competitor needs a boat in order to compete 
 * @author fmittag
 */
public interface CompetitorWithBoat extends Competitor, WithBoat {
    /**
     * Returns a derived short information about a competitor depending on the information available
     * If we have a short name set on the competitor this name will be returned.
     * If no short name exist but a boat the either the sailId or the boat name will returned.
     * If all these attributes have no value null is returned.   
     */
    String getShortInfo();

}
