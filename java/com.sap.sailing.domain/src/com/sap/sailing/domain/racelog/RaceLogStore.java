package com.sap.sailing.domain.racelog;

import java.util.Map;

import com.sap.sailing.domain.base.Fleet;

public interface RaceLogStore {

    RaceLog getRaceLog(RaceLogIdentifier identifier);
    
    Map<Fleet, RaceLog> getRaceLogs(RaceLogIdentifierTemplate template, Iterable<? extends Fleet> fleets);

}
