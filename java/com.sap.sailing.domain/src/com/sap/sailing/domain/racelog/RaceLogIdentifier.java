package com.sap.sailing.domain.racelog;

import java.io.Serializable;

import com.sap.sailing.domain.common.impl.Util.Triple;

public interface RaceLogIdentifier extends Serializable {
    
    String getRaceColumnName();
    
    String getFleetName();

    Triple<String, String, String> getIdentifier();
    
    String getDeprecatedIdentifier();
    
    RaceLogIdentifierTemplate getTemplate();
    
}
