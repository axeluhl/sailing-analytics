package com.sap.sailing.domain.regattalike;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;

/**
 * A domain object that is regatta-like (in other words: similar to a regatta).
 * This is usually a {@link Regatta}. In some special cases however a {@link FlexibleLeaderboard} is used to semantically represent
 * a Regatta - e.g. if a special series uses individual races from other events/regattas.
 * 
 * @author Fredrik Teschke
 *
 */
public interface IsRegattaLike extends Serializable {
    /**
     * @return The RegattaLog associated with this {@code Leaderboard}. Where this log actually lives may be different
     * for different {@code Leaderboard} implementations (i.e. a {@link RegattaLeaderboard} returns the {@code RegattaLog}
     * of its {@link Regatta#getRegattaLog Regatta}, whereas a {@link FlexibleLeaderboard} creates its own {@code RegattaLog}).
     */
    RegattaLog getRegattaLog();
    
    RegattaLikeIdentifier getRegattaLikeIdentifier();
    
    void addListener(RegattaLikeListener listener);
    
    void removeListener(RegattaLikeListener listener);
}