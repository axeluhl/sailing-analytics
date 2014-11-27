package com.sap.sailing.domain.abstractlog.race;

/**
 * Marker interface that all classes implementing {@link RaceLogEvent} should
 * also implement, if the addition of that event should invalidate leaderboard cache.
 * 
 * @author Simon Marcel Pamies
 */
public interface InvalidatesLeaderboardCache {

}
