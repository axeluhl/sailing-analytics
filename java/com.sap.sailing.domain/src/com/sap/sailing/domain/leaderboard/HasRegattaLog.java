package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Regatta;

/**
 * Not all leaderboards can have {@link RegattaLog}s attached. Only such
 * that do should implement this interface.
 * 
 * @author Fredrik Teschke
 *
 */
public interface HasRegattaLog {
    /**
     * @return The RegattaLog associated with this {@code Leaderboard}. Where this log actually lives may be different
     * for different {@code Leaderboard} implementations (i.e. a {@link RegattaLeaderboard} returns the {@code RegattaLog}
     * of its {@link Regatta#getRegattaLog Regatta}, whereas a {@link FlexibleLeaderboard} creates its own {@code RegattaLog}).
     */
    RegattaLog getRegattaLog();
}