package com.sap.sailing.domain.tractracadapter.impl;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.model.lib.api.event.IRace;

public class RaceHandleImpl implements RaceHandle {
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
    public RaceDefinition getRace() {
        return domainFactory.getAndWaitForRaceDefinition(tractracRace.getId());
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
    public RaceDefinition getRace(long timeoutInMilliseconds) {
        return domainFactory.getAndWaitForRaceDefinition(tractracRace.getId(), timeoutInMilliseconds);
    }
    
}
