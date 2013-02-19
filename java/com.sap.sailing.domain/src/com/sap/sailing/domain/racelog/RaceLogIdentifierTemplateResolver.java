package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.racelog.impl.RaceLogOnLeaderboardIdentifier;
import com.sap.sailing.domain.racelog.impl.RaceLogOnRegattaIdentifier;

public interface RaceLogIdentifierTemplateResolver {
    void resolveOnRegattaIdentifier(RaceLogOnRegattaIdentifier identifierTemplate);
    void resolveOnLeaderboardIdentifier(RaceLogOnLeaderboardIdentifier identifierTemplate);
}
