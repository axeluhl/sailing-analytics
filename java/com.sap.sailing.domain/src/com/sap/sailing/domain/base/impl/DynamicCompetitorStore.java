package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.net.URI;

import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

public interface DynamicCompetitorStore extends CompetitorStore {
    @Override
    DynamicCompetitor getExistingCompetitorById(Serializable competitorId);

    @Override
    DynamicCompetitor getOrCreateCompetitor(Serializable competitorId, String name, Color displayColor, String email, URI flagImage, 
            DynamicTeam team, DynamicBoat boat, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag);

    @Override
    Iterable<? extends DynamicCompetitor> getCompetitors();

}
