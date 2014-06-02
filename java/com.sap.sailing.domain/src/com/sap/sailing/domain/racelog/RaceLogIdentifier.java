package com.sap.sailing.domain.racelog;

import java.io.Serializable;

import com.sap.sse.common.UtilNew;

public interface RaceLogIdentifier extends Serializable {
    
    String getRaceColumnName();
    
    String getFleetName();

    UtilNew.Triple<String, String, String> getIdentifier();
    
    String getDeprecatedIdentifier();
    
    RaceLogIdentifierTemplate getTemplate();
    
}
