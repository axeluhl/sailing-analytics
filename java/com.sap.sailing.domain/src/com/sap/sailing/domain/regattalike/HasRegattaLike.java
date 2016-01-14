package com.sap.sailing.domain.regattalike;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;

/**
 * Holds a {@link IsRegattaLike} member. Necessary to deal with the fact that {@link FlexibleLeaderboard}s
 * can also be used to model something like a {@link Regatta}, but are situated at a different level of
 * the domain object hierarchy.
 * @author Fredrik Teschke
 *
 */
public interface HasRegattaLike {
    IsRegattaLike getRegattaLike();
    
    RaceLog getRacelog(String raceColumnName, String fleetName);
    
    /**
     * Determines the competitors registered in the regatta log. Note that this is not necessarily the complete set of
     * competitors participating in this "regatta." For that, use {@link #getAllCompetitors()}.
     */
    Iterable<Competitor> getCompetitorsRegisteredInRegattaLog();
    
    /**
     * Determines the combined set of all competitors from all race columns that this object's {@link IsRegattaLike}
     * has.
     * 
     * @see #getRegattaLike()
     * @see IsRegattaLike#getRaceColumnByName(String)
     * @see RaceColumn#getAllCompetitors()
     */
    Iterable<Competitor> getAllCompetitors();
    
    void registerCompetitor(Competitor competitor);
    void registerCompetitors(Iterable<Competitor> competitor);
    
    void deregisterCompetitor(Competitor competitor);
    void deregisterCompetitors(Iterable<Competitor> competitor);
}
