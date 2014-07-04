package com.sap.sailing.domain.common.racelog.tracking;

public class RaceLogRaceTrackerExistsException extends RuntimeException {

    private static final long serialVersionUID = 6721038282862173471L;

    public RaceLogRaceTrackerExistsException() {
        super();
    }

    public RaceLogRaceTrackerExistsException(String raceLogName) {
        super("Tracker for racelog " + raceLogName + " already exists");
    }
}
