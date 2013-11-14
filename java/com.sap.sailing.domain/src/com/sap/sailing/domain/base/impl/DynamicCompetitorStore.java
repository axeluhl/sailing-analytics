package com.sap.sailing.domain.base.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.CompetitorStore;

public interface DynamicCompetitorStore extends CompetitorStore {
    @Override
    DynamicCompetitor getExistingCompetitorById(Serializable competitorId);

    @Override
    DynamicCompetitor getOrCreateCompetitor(Serializable competitorId, String name, DynamicTeam team, DynamicBoat boat);

    @Override
    Iterable<? extends DynamicCompetitor> getCompetitors();

}
