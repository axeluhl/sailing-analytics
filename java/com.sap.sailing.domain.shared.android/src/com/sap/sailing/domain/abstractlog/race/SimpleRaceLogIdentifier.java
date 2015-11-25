package com.sap.sailing.domain.abstractlog.race;

import java.io.Serializable;

import com.sap.sse.common.Util.Triple;

public interface SimpleRaceLogIdentifier extends Serializable {
    String getRegattaLikeParentName();

    String getRaceColumnName();

    String getFleetName();
    
    Triple<String, String, String> getIdentifier();
}