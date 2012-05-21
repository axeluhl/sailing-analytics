package com.sap.sailing.domain.persistence.impl;

/**
 * Defines literals providing the names for MongoDB collections. The literal documentation described the semantics
 * of the collection identified by that literal.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public enum CollectionNames {
    /**
     * Stores the wind fixes recorded from persistent wind sources.
     */
    WIND_TRACKS,
    
    /**
     * The collection identified by this
     */
    LEADERBOARDS,
    
    LEADERBOARD_GROUPS,
    
    EVENTS,
    
    REGATTAS,
    
    BOAT_CLASSES;
}
