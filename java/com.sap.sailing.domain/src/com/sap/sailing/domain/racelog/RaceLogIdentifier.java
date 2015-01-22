package com.sap.sailing.domain.racelog;

import java.io.Serializable;

import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sse.common.Util;

/**
 * This interfaces serves as a template for the actual RaceLogIdentifier that is compound as follows:
 * <p>
 * {LeaderboardName / RegattaName} + RaceColumnName + FleetName
 * <p>
 */
public interface RaceLogIdentifier extends Serializable {
    
    String getRaceColumnName();
    
    String getFleetName();
    
    RegattaLikeIdentifier getRegattaLikeParent();

    Util.Triple<String, String, String> getIdentifier();
    
    String getDeprecatedIdentifier();
}
