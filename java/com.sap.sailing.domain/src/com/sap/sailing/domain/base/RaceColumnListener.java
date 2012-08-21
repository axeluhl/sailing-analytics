package com.sap.sailing.domain.base;

import java.io.Serializable;

import com.sap.sailing.domain.tracking.TrackedRace;

public interface RaceColumnListener extends Serializable {
    void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace);
    
    void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace);
    
    void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace);
}
