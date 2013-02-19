package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;

public interface RaceLogIdentifierTemplate {
    
    String getHostName();
    
    RaceLogIdentifier compile(RaceColumn column, Fleet fleet);
    
    void resolve(RaceLogIdentifierTemplateResolver resolver);
}
