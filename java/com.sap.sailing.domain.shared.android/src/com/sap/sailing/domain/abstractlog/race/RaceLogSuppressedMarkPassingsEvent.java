package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.Revokable;

/**
 * No need to implement {@link InvalidatesLeaderboardCache} as changing mark passings is already notified to
 * the race change listener from where it invalidates the leaderboard cache accordingly.
 */
public interface RaceLogSuppressedMarkPassingsEvent extends RaceLogEvent, Revokable {
    
    public Integer getZeroBasedIndexOfFirstSuppressedWaypoint();

}
