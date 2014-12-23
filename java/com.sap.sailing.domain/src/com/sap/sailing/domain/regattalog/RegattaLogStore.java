package com.sap.sailing.domain.regattalog;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;


public interface RegattaLogStore {
    RegattaLog getRegattaLog(RegattaLikeIdentifier regattaLikeId, boolean ignoreCache);
    
    /**
     * Removes all events stored for the log identified by {@code parentObjectName} from the database and
     * from the cache.
     */
    void removeRegattaLog(RegattaLikeIdentifier regattaLikeId);
}
