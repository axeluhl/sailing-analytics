package com.sap.sailing.racecommittee.app.domain.state;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;


/**
 * Interfaces providing methods to be called when a {@link RaceState} has changed somehow.
 */
public interface RaceStateChangedListener {
    
    /**
     * Called when the {@link RaceLogRaceStatus} of the state has changed.
     * @param state that changed its status.
     */
    void onRaceStateStatusChanged(RaceState state);
    
    /**
     * Called when the course of the state has changed.
     * @param state that changed its course.
     */
    void onRaceStateCourseDesignChanged(RaceState state);
    
    /**
     * Called when the protest start time of the state has changed.
     * @param state that changed its protest time.
     */
    void onRaceStateProtestStartTimeChanged(RaceState state);
}