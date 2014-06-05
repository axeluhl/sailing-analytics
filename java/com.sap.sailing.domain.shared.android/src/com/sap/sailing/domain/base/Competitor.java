package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.WithID;
import com.sap.sse.datamining.shared.annotations.Connector;

public interface Competitor extends Named, WithID, IsManagedBySharedDomainFactory {
    @Connector
    Team getTeam();

    @Connector
    Boat getBoat();
    
    Color getColor();
}
