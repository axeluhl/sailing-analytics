package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;

/**
 * Marker interface that all classes implementing {@link RaceLogEvent} or {@link RegattaLogEvent} should also implement,
 * if the addition of that event should invalidate leaderboard cache.
 * 
 * @author Simon Marcel Pamies
 */
public interface InvalidatesLeaderboardCache {

}
