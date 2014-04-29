package com.sap.sailing.domain.tractracadapter.impl;

import java.util.Collections;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.model.lib.api.event.IRace;

public class RaceHandleImpl implements RacesHandle {
    private final IRace tractracRace;
    private final DomainFactory domainFactory;
    private final DynamicTrackedRegatta trackedRegatta;
    private final RaceTracker raceTracker;
    
    public RaceHandleImpl(DomainFactory domainFactory, IRace tractracRace, DynamicTrackedRegatta trackedRegatta, RaceTracker raceTracker) {
        this.domainFactory = domainFactory;
        this.tractracRace = tractracRace;
        this.trackedRegatta = trackedRegatta;
        this.raceTracker = raceTracker;
    }

    @Override
    public com.sap.sailing.domain.base.Regatta getRegatta() {
        return trackedRegatta.getRegatta();
    }

    @Override
    public Set<RaceDefinition> getRaces() {
        Set<RaceDefinition> result = Collections.singleton(domainFactory.getAndWaitForRaceDefinition(tractracRace.getId()));
        return result;
    }
    
    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
    }

    @Override
    public RaceTracker getRaceTracker() {
        return raceTracker;
    }

    @Override
    public Set<RaceDefinition> getRaces(long timeoutInMilliseconds) {
        final Set<RaceDefinition> result;
        final RaceDefinition raceDefinition = domainFactory.getAndWaitForRaceDefinition(race.getId(), timeoutInMilliseconds);
        if (raceDefinition != null) { // may have time-outed
            result = Collections.singleton(raceDefinition);
        } else {
            result = Collections.emptySet();
        }
        return result;
    }
    
}
