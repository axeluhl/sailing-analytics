package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

public interface Competitor extends Named, WithID, IsManagedByDomainFactory {
    Team getTeam();

    Boat getBoat();

}
