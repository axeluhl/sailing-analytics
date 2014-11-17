package com.sap.sailing.domain.abstractlog.race.scoring;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.InvalidatesLeaderboardCache;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;

/**
 * Event that could possibly overwrite scoring for a leaderboard column. The idea
 * is that the scoring scheme checks if such an event is available and if it applies
 * extended scoring rules. The additional information contains a type that indicates
 * which action should be executed.
 * 
 * @author Simon Marcel Pamies
 */
public interface AdditionalScoringInformationEvent extends RaceLogEvent, Revokable, InvalidatesLeaderboardCache {
    
    /**
     * @return the type of information this event holds
     */
    AdditionalScoringInformationType getType();
    
}
