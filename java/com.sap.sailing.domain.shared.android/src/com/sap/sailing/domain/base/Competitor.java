package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.WithID;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface Competitor extends Named, WithID, IsManagedBySharedDomainFactory {
    @SideEffectFreeValue(messageKey="Team")
    Team getTeam();

    @SideEffectFreeValue(messageKey="Boat")
    Boat getBoat();
    
    Color getColor();
}
