package com.sap.sailing.domain.base;

/**
 * The name shall be unique across all events tracked concurrently. In particular, if you want to
 * keep apart events in different boat classes, make sure the boat class name becomes part of the
 * event name.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Event extends Named {
    Iterable<RaceDefinition> getAllRaces();
    
    BoatClass getBoatClass();
    
    Iterable<Competitor> getCompetitors();

    void addRace(RaceDefinition race);

    void removeRace(RaceDefinition raceDefinition);
    
}
