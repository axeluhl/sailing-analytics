package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;

public interface RaceLogResolver {
    RaceLog resolve(SimpleRaceLogIdentifier identifier) throws RegataLikeNameOfIdentifierDoesntMatchActualRegattaLikeNameException;
}
