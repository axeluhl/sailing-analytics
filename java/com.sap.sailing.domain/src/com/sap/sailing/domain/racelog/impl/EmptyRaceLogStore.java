package com.sap.sailing.domain.racelog.impl;


import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;

public enum EmptyRaceLogStore implements RaceLogStore {
    INSTANCE;

    @Override
    public RaceLog getRaceLog(RaceLogIdentifier identifier) {
        return new RaceLogImpl(UUID.randomUUID().toString());
    }

	@Override
	public Map<Fleet, RaceLog> getRaceLogs(RaceLogIdentifierTemplate template, Iterable<? extends Fleet> fleets) {
		return Collections.emptyMap();
	}

}
