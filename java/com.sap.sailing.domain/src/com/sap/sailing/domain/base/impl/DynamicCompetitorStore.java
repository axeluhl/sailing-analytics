package com.sap.sailing.domain.base.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sse.common.Color;

public interface DynamicCompetitorStore extends CompetitorStore {
    @Override
    DynamicCompetitor getExistingCompetitorById(Serializable competitorId);

    @Override
    DynamicCompetitor getOrCreateCompetitor(Serializable competitorId, String name, Color displayColor, DynamicTeam team, DynamicBoat boat);

    @Override
    Iterable<? extends DynamicCompetitor> getCompetitors();

}
