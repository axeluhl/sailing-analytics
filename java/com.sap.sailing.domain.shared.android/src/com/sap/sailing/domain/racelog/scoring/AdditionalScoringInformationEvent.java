package com.sap.sailing.domain.racelog.scoring;

import com.sap.sailing.domain.racelog.InvalidatesLeaderboardCache;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.Revokable;

/**
 * Event that could possibly overwrite scoring for a leaderboard column. The idea
 * is that the scoring scheme checks if such an event is available and if it applies
 * extended scoring rules.
 * 
 * @author Simon Marcel Pamies
 */
public interface AdditionalScoringInformationEvent extends RaceLogEvent, Revokable, InvalidatesLeaderboardCache {
    
}
