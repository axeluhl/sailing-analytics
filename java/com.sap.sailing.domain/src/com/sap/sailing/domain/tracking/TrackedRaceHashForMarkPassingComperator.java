package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Position;
import com.sap.sse.common.TimePoint;

/**
 * This comperator is used to calculate a set of hashes for a {@link trackedRace} 
 * in order to lay the foundation to decide if the {@link markpassings} are recalculated or
 * loaded out of the database, when restarting a server.
 * 
 * @author Fabian Kallenbach (I550803)
 *
 */

public interface TrackedRaceHashForMarkPassingComperator {
    /**
     * This Method loads the individual hashes into the local hashmap and then {@link #setHashValuesForMarkPassingCalculation} in {@link TrackedRace}.
     */
    void CalculateHash();
    
    /**
     * Calculates the hash for the {@link competitor} on the basis of it's ID.
     */
    int CalculateHashForCompetitor (Competitor c);
    
    /**
     * Calculates the hash for the {@link boat} on the basis of it's ID.
     */
    int CalculateHashForBoat (Boat b);
    
    /**
     * Calculates the hash for the race start on the basis of {@link #getStartOfTracking} and if it isn't inferred also on the {@link #getStartOfRace}.
     */
    int CalculateHashForStart ();
    
    /**
     * Calculates the hash for the race end on the basis of {@link #getEndOfTracking}.
     */
    int CalculateHashForEnd ();
    
    /**
     * Calculates the hash for the number of GPSFixes of the {@link trackedRace}. 
     */
    int CalculateHashForNumberOfGPSFixes ();
    
    /**
     * Calculates the hash for all {@link GPSFix} on the basis of theire {@link TimePoint}s and {@link Position}s.
     */
    int CalculateHashForGPSFixes ();
    
    /**
     * Calculates the hash for a {@link TimePoint}.
     */
    int CalculateHashForTimePoint (TimePoint tp);
    
    /**
     * Calculates the hash for a {@link Position}.
     */
    int CalculateHashForPosition (Position p);
    
    /**
     * Calculates the hash for a {@link Waypoint}.
     */
    int CalculateHashForWaypoints ();
}
