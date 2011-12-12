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
    
    /**
     * @return <code>null</code>, if this event does not contain a race (see {@link #getAllRaces}) whose
     * {@link RaceDefinition#getName()} equals <code>raceName</code>
     */
    RaceDefinition getRaceByName(String raceName);
    
    BoatClass getBoatClass();
    
    Iterable<Competitor> getCompetitors();

    void addRace(RaceDefinition race);

    void removeRace(RaceDefinition raceDefinition);
    
}
