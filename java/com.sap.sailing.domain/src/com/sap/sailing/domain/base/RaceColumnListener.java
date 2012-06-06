package com.sap.sailing.domain.base;

import com.sap.sailing.domain.tracking.TrackedRace;

public interface RaceColumnListener {
    void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace);
    
    void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace);
}
