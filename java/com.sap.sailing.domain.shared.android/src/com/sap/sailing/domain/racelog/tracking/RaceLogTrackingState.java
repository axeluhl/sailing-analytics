package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.racelog.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;

/**
 * The state of the race tracked via the racelog. This can be determined by inspecting the corresponding events
 * in the racelog with the {@link RaceLogTrackingStateAnalyzer}.
 * 
 * {@link #TRACKING}: a {@link StartTrackingEvent} is present
 * {@link #AWAITING_RACE_DEFINITION}: a {@link DenoteForTrackingEvent} is present, but no {@link StartTrackingEvent} yet.
 * {@link #NOT_A_RACELOG_TRACKED_RACE}: no {@link DenoteForTrackingEvent} is present
 * @author Fredrik Teschke
 *
 */
public enum RaceLogTrackingState {
    //can't determine whether tracking has ended from race log, therefore omitting TRACKING_ENDED state for now
    NOT_A_RACELOG_TRACKED_RACE, AWAITING_RACE_DEFINITION, TRACKING;

    public boolean isForTracking() {
        return this != NOT_A_RACELOG_TRACKED_RACE;
    }
}
