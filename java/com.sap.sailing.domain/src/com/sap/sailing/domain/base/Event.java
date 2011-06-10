package com.sap.sailing.domain.base;

public interface Event extends Named {
    Iterable<RaceDefinition> getAllRaces();
    
    BoatClass getBoatClass();
    
    Iterable<Competitor> getCompetitors();

    void addRace(RaceDefinition race);
    
}
