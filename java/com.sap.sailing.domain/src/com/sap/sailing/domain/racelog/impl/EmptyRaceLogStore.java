package com.sap.sailing.domain.racelog.impl;


import java.util.UUID;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;

public enum EmptyRaceLogStore implements RaceLogStore {
    INSTANCE;

    @Override
    public RaceLog getRaceLog(RaceLogIdentifier identifier) {
        return new RaceLogImpl(UUID.randomUUID().toString());
    }

}
