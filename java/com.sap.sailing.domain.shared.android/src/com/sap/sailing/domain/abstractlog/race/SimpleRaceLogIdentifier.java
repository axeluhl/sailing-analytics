package com.sap.sailing.domain.abstractlog.race;

import com.sap.sse.common.Util.Triple;

public interface SimpleRaceLogIdentifier {

    String getRegattaLikeParentName();

    String getRaceColumnName();

    String getFleetName();
    
    Triple<String, String, String> getIdentifier();
    
    String getDeprecatedIdentifier();

}