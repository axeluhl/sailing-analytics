package com.sap.sailing.domain.base;

public interface EventListener {
    void raceAdded(Event event, RaceDefinition race);
    void raceRemoved(Event event, RaceDefinition race);
}
