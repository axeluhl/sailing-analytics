package com.sap.sailing.domain.base;


public interface RegattaListener {
    default void raceAdded(Regatta regatta, RaceDefinition race) {}
    default void raceRemoved(Regatta regatta, RaceDefinition race) {}
    default void useStartTimeInferenceChanged(Regatta regatta, boolean newUseStartTimeInference) {}
    default void controlTrackingFromStartAndFinishTimesChanged(Regatta regatta, boolean newControlTrackingFromStartAndFinishTimes) {}
    default void autoRestartTrackingUponCompetitorSetChangeChanged(Regatta regatta, boolean newAutoRestartTrackingUponCompetitorSetChange) {}
}
