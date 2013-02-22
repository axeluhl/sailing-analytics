package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.base.Fleet;

public interface RaceLogIdentifierTemplate {
    
    String getHostName();
    
    RaceLogIdentifier compile(Fleet fleet);
    
    void resolve(RaceLogIdentifierTemplateResolver resolver);
}
