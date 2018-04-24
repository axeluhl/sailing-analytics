package com.sap.sailing.domain.base.impl;

import java.net.URI;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Renamable;

public interface DynamicCompetitor extends Competitor, Renamable {
    DynamicTeam getTeam();

    void setColor(Color displayColor);

    void setEmail(String email);

    void setShortName(String shortName);

    void setSearchTag(String searchTag);

    void setFlagImage(URI flagImage);

    void setTimeOnTimeFactor(Double timeOnTimeFactor);

    void setTimeOnDistanceAllowancePerNauticalMile(Duration timeOnDistanceAllowancePerNauticalMile);
}
