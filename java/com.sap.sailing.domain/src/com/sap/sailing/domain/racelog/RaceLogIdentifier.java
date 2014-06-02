package com.sap.sailing.domain.racelog;

import java.io.Serializable;

import com.sap.sse.common.Util;

public interface RaceLogIdentifier extends Serializable {
    
    String getRaceColumnName();
    
    String getFleetName();

    Util.Triple<String, String, String> getIdentifier();
    
    String getDeprecatedIdentifier();
    
    RaceLogIdentifierTemplate getTemplate();
    
}
