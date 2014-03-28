package com.sap.sailing.domain.tractracadapter.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;

public class RaceHandleImpl implements RacesHandle {
    private final IEvent tractracEvent;
    private final DomainFactory domainFactory;
    private final DynamicTrackedRegatta trackedRegatta;
    private final RaceTracker raceTracker;
    
    public RaceHandleImpl(DomainFactory domainFactory, IEvent tractracEvent, DynamicTrackedRegatta trackedRegatta, RaceTracker raceTracker) {
        this.domainFactory = domainFactory;
        this.tractracEvent = tractracEvent;
        this.trackedRegatta = trackedRegatta;
        this.raceTracker = raceTracker;
    }

    @Override
    public com.sap.sailing.domain.base.Regatta getRegatta() {
        return trackedRegatta.getRegatta();
    }

    @Override
    public Set<RaceDefinition> getRaces() {
        Set<RaceDefinition> result = new HashSet<RaceDefinition>();
        for (IRace r : SynchronizationUtil.getRaces(tractracEvent)) {
            result.add(domainFactory.getAndWaitForRaceDefinition(r.getId()));
        }
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
        Set<RaceDefinition> result = new HashSet<RaceDefinition>();
        for (IRace race : SynchronizationUtil.getRaces(tractracEvent)) {
            result.add(domainFactory.getAndWaitForRaceDefinition(race.getId(), timeoutInMilliseconds));
        }
        return result;
    }
    
}
