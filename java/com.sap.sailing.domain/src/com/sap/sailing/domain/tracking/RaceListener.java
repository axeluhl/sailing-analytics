package com.sap.sailing.domain.tracking;

public interface RaceListener {
    void raceAdded(TrackedRace trackedRace);

    void raceRemoved(TrackedRace trackedRace);
}
