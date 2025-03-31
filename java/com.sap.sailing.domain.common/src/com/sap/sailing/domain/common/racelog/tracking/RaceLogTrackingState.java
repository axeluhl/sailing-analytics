package com.sap.sailing.domain.common.racelog.tracking;


/**
 * The state of the race tracked via the racelog. This can be determined by inspecting the corresponding events
 * in the racelog with the {@link RaceLogTrackingStateAnalyzer}.
 * @author Fredrik Teschke
 *
 */
public enum RaceLogTrackingState {
    //can't determine whether tracking has ended from race log, therefore omitting TRACKING_ENDED state for now
    /**
     * no {@link DenoteForTrackingEvent} is present
     */
    NOT_A_RACELOG_TRACKED_RACE,
    
    /**
     * a {@link DenoteForTrackingEvent} is present, but no {@link StartTrackingEvent} yet.
     */
    AWAITING_RACE_DEFINITION,
    
    /**
     * a {@link StartTrackingEvent} is present
     */
    TRACKING;

    public boolean isForTracking() {
        return this != NOT_A_RACELOG_TRACKED_RACE;
    }
    
    public boolean isTracking() {
        return this == TRACKING;
    }
}
