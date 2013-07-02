package com.sap.sailing.domain.racelog;


public interface RaceLogStore {

    RaceLog getRaceLog(RaceLogIdentifier identifier, boolean ignoreCache);

    void removeListenersAddedByStoreFrom(RaceLog raceLogAvailable);

}
