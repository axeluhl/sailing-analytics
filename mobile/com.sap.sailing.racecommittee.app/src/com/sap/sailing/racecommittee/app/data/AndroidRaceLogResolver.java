package com.sap.sailing.racecommittee.app.data;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RegataLikeNameOfIdentifierDoesntMatchActualRegattaLikeNameException;

public class AndroidRaceLogResolver implements RaceLogResolver {

    @Override
    public RaceLog resolve(SimpleRaceLogIdentifier identifier)
            throws RegataLikeNameOfIdentifierDoesntMatchActualRegattaLikeNameException {

        //has to check against local as well as online racelog events
        
        //FIXME: somehow get the ManagedRace belonging to identifier in order to get the RaceLog
        //InMemoryDataStore.INSTANCE.(new ManagedRaceIdentifierImpl(identifier.getRaceColumnName(), new FleetIdentifier(
        
        
        return null;
    }

}
