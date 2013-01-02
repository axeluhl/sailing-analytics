package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.WithID;

public interface Competitor extends Named, WithID, IsManagedByDomainFactory {
    Team getTeam();

    Boat getBoat();

}
