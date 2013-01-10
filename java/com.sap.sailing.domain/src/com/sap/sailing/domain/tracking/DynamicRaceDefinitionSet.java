package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.RaceDefinition;

/**
 * Particularly the way the TracTrac connector was originally designed, the set of {@link RaceDefinition} objects
 * received from connecting to a particular TracTrac event is not known up-front. We even wait (for historic reasons,
 * because the course definition used to be immutable) for the course to be received before creating the
 * {@link RaceDefinition} object. Therefore, a {@link RaceTracker#getRaces()} call needs to understand which
 * {@link RaceDefinition} objects were created so far.<p>
 * 
 * This interface is used particularly by the race course receiver in the TracTrac case to update a tracker's set
 * of {@link RaceDefinition}s.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DynamicRaceDefinitionSet {
    void addRaceDefinition(RaceDefinition race);
}
