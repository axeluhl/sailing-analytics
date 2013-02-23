package com.sap.sailing.domain.racelog;

import java.io.Serializable;

import com.sap.sailing.domain.base.Fleet;

public interface RaceLogIdentifierTemplate extends Serializable {
    
    String getHostName();
    
    RaceLogIdentifier compile(Fleet fleet);
    
    void resolve(RaceLogIdentifierTemplateResolver resolver);
}
