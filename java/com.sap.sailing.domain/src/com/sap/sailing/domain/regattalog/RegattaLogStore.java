package com.sap.sailing.domain.regattalog;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sse.common.Named;


public interface RegattaLogStore {
    /**
     * @param parentObjectName This is the {@link Named#getName name} of the domain object
     * that manages the {@code RegattaLog} - either a {@link Regatta} or {@link FlexibleLeaderboard}.
     * The {@link RaceLogIdentifierTemplate} internally also only uses the name of these elements to
     * identify them.
     * Clashes are avoided only on UI level, where unique leaderboard names are enforced. As a RegattaLeaderboard
     * always carries the name of its Regatta this is somewhat safe.
     */
    RegattaLog getRegattaLog(String parentObjectName, boolean ignoreCache);
    
    /**
     * Removes all events stored for the log identified by {@code parentObjectName} from the database and
     * from the cache.
     */
    void removeRegattaLog(String parentObjectName);

    void removeListenersAddedByStoreFrom(RegattaLog regattaLog);

}
