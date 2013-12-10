package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.Renamable;

public interface DynamicCompetitor extends Competitor, Renamable {
    DynamicBoat getBoat();
    DynamicTeam getTeam();
    
    void setColor(Color displayColor);
}
