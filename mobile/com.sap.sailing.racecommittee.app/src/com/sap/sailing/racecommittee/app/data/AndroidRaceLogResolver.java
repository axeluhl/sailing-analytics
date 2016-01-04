package com.sap.sailing.racecommittee.app.data;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class AndroidRaceLogResolver implements RaceLogResolver {

    @Override
    public RaceLog resolve(SimpleRaceLogIdentifier identifier) {
        final ManagedRace race = InMemoryDataStore.INSTANCE.getRace(identifier);
        return race == null ? null : race.getRaceLog();
    }

}
