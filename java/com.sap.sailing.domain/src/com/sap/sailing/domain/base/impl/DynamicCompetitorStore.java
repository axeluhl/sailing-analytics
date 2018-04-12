package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.net.URI;

import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

public interface DynamicCompetitorStore extends CompetitorAndBoatStore {
    @Override
    DynamicCompetitor getExistingCompetitorById(Serializable competitorId);

    @Override
    DynamicCompetitor getOrCreateCompetitor(Serializable competitorId, String name, String shortName, Color displayColor, String email, URI flagImage, 
            DynamicTeam team, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag);

    @Override
    Iterable<? extends DynamicCompetitor> getAllCompetitors();

}
