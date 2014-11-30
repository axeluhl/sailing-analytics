package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.abstractlog.race.RaceLog;


public interface RaceLogStore {

    RaceLog getRaceLog(RaceLogIdentifier identifier, boolean ignoreCache);
    
    /**
     * Removes all events stored for the race log identified by <code>identifier</code> from the database and
     * from the cache.
     */
    void removeRaceLog(RaceLogIdentifier identifier);

    void removeListenersAddedByStoreFrom(RaceLog raceLogAvailable);

}
