package com.sap.sailing.domain.base;

public interface RaceDefinition extends Named {
    BoatClass getBoatClass();
    
    Course getCourse();

    Iterable<Competitor> getCompetitors();
}
