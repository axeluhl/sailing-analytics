package com.sap.sailing.domain.base;


public interface RegattaListener {
    void raceAdded(Regatta regatta, RaceDefinition race);
    void raceRemoved(Regatta regatta, RaceDefinition race);
}
