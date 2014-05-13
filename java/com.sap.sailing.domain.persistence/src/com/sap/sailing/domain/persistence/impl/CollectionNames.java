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
     * Stores the leaderboards with their names, score corrections, competitor display name overrides, race
     * columns and their fleets as well as the per-fleet tracked race assignments.
     */
    LEADERBOARDS,
    
    /**
     * Stores the leaderboard group configurations with references to the {@link #LEADERBOARDS} collection
     */
    LEADERBOARD_GROUPS,
    
    /**
     * Top-level event information about events such as Kiel Week 2011, or IDM Travem�nde 2011, including name and
     * course areas.
     */
    EVENTS,
    
    /** 
     * Stores the registered sailing servers.
     */
    SAILING_SERVERS,
    
    /**
     * Stores regatta definitions including their series layout and fleets and race columns. Regattas can reference
     * the event from the {@link #EVENTS} collection to which they belong.
     */
    REGATTAS,
    
    /**
     * Stores boat class-specific master data such as the class's hull length, logo, name, number of sailors, etc.
     * To be implemented in future versions.
     */
    BOAT_CLASSES,
    
    /**
     * Stores the mapping of {@link RaceDefinition#getId race IDs} to regatta names for automatic re-association
     * when tracking races again without explicitly specifying a regatta.
     */
    REGATTA_FOR_RACE_ID, 
    
    /**
     * Stores the race log events for a tracked race.
     */

    
    /**
     * Stores competitors for smartphone tracking.
     */
    COMPETITORS,
    RACE_LOGS,
    
    /**
     * Stores configurations for mobile devices.
     */
    CONFIGURATIONS,
    
    /**
     * Stores {@link GPSFix}es
     */
    GPS_FIXES,
}
