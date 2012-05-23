package com.sap.sailing.domain.tractracadapter.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracRaceTracker;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.Race;

public class RaceHandleImpl implements RacesHandle {
    private final Event tractracEvent;
    private final DomainFactory domainFactory;
    private final DynamicTrackedRegatta trackedRegatta;
    private final TracTracRaceTracker raceTracker;
    
    public RaceHandleImpl(DomainFactory domainFactory, Event tractracEvent, DynamicTrackedRegatta trackedRegatta, TracTracRaceTracker raceTracker) {
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
        for (Race r : tractracEvent.getRaceList()) {
            result.add(domainFactory.getAndWaitForRaceDefinition(r));
        }
        return result;
    }
    
    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
    }

    @Override
    public TracTracRaceTracker getRaceTracker() {
        return raceTracker;
    }

    @Override
    public Set<RaceDefinition> getRaces(long timeoutInMilliseconds) {
        Set<RaceDefinition> result = new HashSet<RaceDefinition>();
        for (Race r : tractracEvent.getRaceList()) {
            result.add(domainFactory.getAndWaitForRaceDefinition(r, timeoutInMilliseconds));
        }
        return result;
    }
    
}
