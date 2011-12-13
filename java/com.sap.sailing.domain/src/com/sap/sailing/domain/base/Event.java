package com.sap.sailing.domain.base;

import com.sap.sailing.domain.tracking.TrackedEvent;

/**
 * The name shall be unique across all events tracked concurrently. In particular, if you want to
 * keep apart events in different boat classes, make sure the boat class name becomes part of the
 * event name.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Event extends Named {
    /**
     * Please note that the {@link RaceDefinition}s of the {@link Event} must not be synchronized {@link RaceDefinition}s
     * of {@link TrackedEvent}. The values could be inconsistent.
     * @return
     */
    Iterable<RaceDefinition> getAllRaces();
    
    /**
     * Please note that the {@link RaceDefinition} of {@link Event} must not be synchronized with the 
     * {@link RaceDefinition} of {@link TrackedEvent}. The values could be inconsistent.
     * 
     * @return <code>null</code>, if this event does not contain a race (see {@link #getAllRaces}) whose
     * {@link RaceDefinition#getName()} equals <code>raceName</code>
     */
    RaceDefinition getRaceByName(String raceName);
    
    BoatClass getBoatClass();
    
    Iterable<Competitor> getCompetitors();

    void addRace(RaceDefinition race);

    void removeRace(RaceDefinition raceDefinition);
    
}
