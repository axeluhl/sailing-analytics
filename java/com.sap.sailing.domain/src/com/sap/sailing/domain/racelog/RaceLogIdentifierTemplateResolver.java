package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.racelog.impl.RaceLogOnLeaderboardIdentifier;
import com.sap.sailing.domain.racelog.impl.RaceLogOnRegattaIdentifier;

public interface RaceLogIdentifierTemplateResolver {
    void resolveOnRegattaIdentifier(RaceLogOnRegattaIdentifier raceLogStoreOnRegattaIdentifier);
    void resolveOnLeaderboardIdentifier(RaceLogOnLeaderboardIdentifier raceLogStoreOnLeaderboardIdentifier);
}
