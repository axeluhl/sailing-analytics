package com.sap.sailing.domain.racelog.tracking;

public enum RaceLogTrackingState {
    //can't determine whether tracking has ended from race log, therefore omitting TRACKING_ENDED state for now
    NOT_A_RACELOG_TRACKED_RACE, AWAITING_RACE_DEFINITION, TRACKING;

    public boolean isForTracking() {
        return this != NOT_A_RACELOG_TRACKED_RACE;
    }
}
