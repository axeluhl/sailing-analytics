package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.WithID;

public interface Competitor extends Named, WithID, IsManagedBySharedDomainFactory {
    Team getTeam();

    Boat getBoat();
    
    Color getColor();
}
