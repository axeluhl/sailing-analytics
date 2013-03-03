package com.sap.sailing.racecommittee.app.domain.state;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;

public interface RaceState {

    /**
     * Register a new listener on state changes.
     * 
     * @param listener
     *            to be registered.
     */
    void registerListener(RaceStateChangedListener listener);

    /**
     * Unregister a previously registered state change listener.
     * 
     * @param listener
     *            to be unregistered.
     */
    void unregisterListener(RaceStateChangedListener listener);

    /**
     * @return the log of the race.
     */
    RaceLog getRaceLog();

    /**
     * Gets the current race's start time.
     * 
     * @return the start time or <code>null</code>.
     */
    TimePoint getStartTime();

    /**
     * Sets the current race's start time
     * 
     * @param eventTime
     *            time of this event.
     * @param newStartTime
     *            to be set.
     */
    void setStartTime(TimePoint evenTime, TimePoint newStartTime);

    /**
     * Updates the race's status.
     * 
     * @return the new status, as returned by {@link RaceState#getStatus()}.
     */
    RaceLogRaceStatus updateStatus();

    /**
     * @return the status of the race.
     */
    RaceLogRaceStatus getStatus();

    /**
     * TODO: remove when start procedures are ready
     */
    void onRaceStarted(TimePoint eventTime);

}
