package com.sap.sailing.domain.racelog;

import java.io.Serializable;

public interface RaceLogIdentifier extends Serializable {
    
    String getRaceColumnName();
    
    String getFleetName();

    Serializable getIdentifier();
    
    RaceLogIdentifierTemplate getTemplate();
    
}
