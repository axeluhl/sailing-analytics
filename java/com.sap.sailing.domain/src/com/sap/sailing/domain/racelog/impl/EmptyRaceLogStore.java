package com.sap.sailing.domain.racelog.impl;


import java.util.UUID;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;

public enum EmptyRaceLogStore implements RaceLogStore {
    INSTANCE;

    @Override
    public RaceLog getRaceLog(RaceLogIdentifier identifier, boolean ignoreCache) {
        return new RaceLogImpl(UUID.randomUUID().toString(), identifier.getIdentifier());
    }

    @Override
    public void removeListenersAddedByStoreFrom(RaceLog raceLogAvailable) {
    }

    @Override
    public void removeRaceLog(RaceLogIdentifier identifier) {
    }
}
