package com.sap.sailing.domain.base.impl;

import java.net.URI;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

public interface DynamicCompetitor extends Competitor, Renamable {
    DynamicBoat getBoat();

    DynamicTeam getTeam();

    void setColor(Color displayColor);

    void setEmail(String email);
    
    void setFlagImage(URI flagImage);

    void setTimeOnTimeFactor(Double timeOnTimeFactor);

    void setTimeOnDistanceAllowancePerNauticalMile(Duration timeOnDistanceAllowancePerNauticalMile);
}
