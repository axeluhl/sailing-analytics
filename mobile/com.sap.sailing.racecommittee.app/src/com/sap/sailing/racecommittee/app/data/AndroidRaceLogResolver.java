package com.sap.sailing.racecommittee.app.data;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RegataLikeNameOfIdentifierDoesntMatchActualRegattaLikeNameException;

public class AndroidRaceLogResolver implements RaceLogResolver {

    @Override
    public RaceLog resolve(SimpleRaceLogIdentifier identifier)
            throws RegataLikeNameOfIdentifierDoesntMatchActualRegattaLikeNameException {
        return InMemoryDataStore.INSTANCE.getRace(identifier).getRaceLog();
    }

}
