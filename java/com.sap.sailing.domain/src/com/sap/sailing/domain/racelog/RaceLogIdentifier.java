package com.sap.sailing.domain.racelog;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;

/**
 * This interfaces serves as a template for the actual RaceLogIdentifier that is compound as follows:
 * <p>
 * {LeaderboardName / RegattaName} + RaceColumnName + FleetName
 * <p>
 */
public interface RaceLogIdentifier extends Serializable, SimpleRaceLogIdentifier {
    RegattaLikeIdentifier getRegattaLikeParent();
}
