package com.sap.sailing.domain.regattalike;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RegataLikeNameOfIdentifierDoesntMatchActualRegattaLikeNameException;
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
    
    RaceLog getRacelog(SimpleRaceLogIdentifier identifier) throws RegataLikeNameOfIdentifierDoesntMatchActualRegattaLikeNameException;
}
