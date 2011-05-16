package com.sap.sailing.domain.base;

public interface Event extends Named {
    Iterable<RaceDefinition> getAllRaces();
    
    Iterable<RaceDefinition> getAllRaces(BoatClass boatClass);
    
    Iterable<Course> getCourses();
    
    Iterable<BoatClass> getClasses();
    
    Iterable<Competitor> getCompetitors();
    
}
