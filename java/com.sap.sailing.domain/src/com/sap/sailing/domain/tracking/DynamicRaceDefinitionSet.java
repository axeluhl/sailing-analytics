package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.RaceDefinition;

/**
 * Particularly the way the TracTrac connector was originally designed, the set of {@link RaceDefinition} objects
 * received from connecting to a particular TracTrac event is not known up-front. We even wait (for historic reasons,
 * because the course definition used to be immutable) for the course to be received before creating the
 * {@link RaceDefinition} object. Therefore, a {@link RaceTracker#getRace()} call needs to understand which
 * {@link RaceDefinition} objects were created so far.<p>
 * 
 * This interface is used particularly by the race course receiver in the TracTrac case to update a tracker's set
 * of {@link RaceDefinition}s.
 * 
 * @author Axel Uhl (d043530)
 *
 */
@FunctionalInterface
public interface DynamicRaceDefinitionSet {
    void addRaceDefinition(RaceDefinition race, DynamicTrackedRace trackedRace);
    
    /**
     * Called when the race has not or can not be loaded. A {@code reason} may be provided or may be left {@code null}.
     * This default implementation does nothing. Implementing classes may choose, e.g., to stop all further efforts
     * trying to track the race.
     */
    default void raceNotLoaded(String reason) throws Exception {}
}
